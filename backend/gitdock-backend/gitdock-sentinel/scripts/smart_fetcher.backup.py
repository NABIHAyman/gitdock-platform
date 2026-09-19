import os
import json
import time
import requests
import logging
import threading
import pika
from confluent_kafka import Producer
from dotenv import load_dotenv

from datetime import datetime, timedelta, timezone


load_dotenv()

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

KAFKA_BROKER = f"{os.getenv('KAFKA_HOST', '127.0.0.1')}:{os.getenv('KAFKA_PORT', 9092)}"
TOPIC_NAME = "cleaned-commits"
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "ubuntu-host")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", 5672))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")

# Liste dynamique des repos à surveiller (thread-safe)
target_repos = set()
repos_lock = threading.Lock()

HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json"
}
DIFF_HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3.diff"
}


# --- THREAD 1 : Écoute RabbitMQ pour les nouveaux projets ---
def rabbitmq_listener():
    logger.info(f"🐰 Démarrage de l'écoute RabbitMQ sur {RABBITMQ_HOST} pour de nouvelles cibles...")
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        connection = pika.BlockingConnection(pika.ConnectionParameters(
            host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=credentials))
        channel = connection.channel()

        # On se branche sur la queue de synchronisation existante de GitDock
        queue_name = 'gitdock.sync.request.queue'
        channel.queue_declare(queue=queue_name, durable=True)

        def callback(ch, method, properties, body):
            data = json.loads(body)
            repo_url = data.get("repoUrl", "")

            # Extraction du format "proprietaire/repo" depuis l'URL GitHub
            if "github.com" in repo_url:
                parts = repo_url.rstrip('/').split('/')
                repo_path = f"{parts[-2]}/{parts[-1]}"

                with repos_lock:
                    target_repos.add(repo_path)
                logger.info(f"🎯 NOUVELLE CIBLE VERROUILLÉE : {repo_path}")

            ch.basic_ack(delivery_tag=method.delivery_tag)

        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(queue=queue_name, on_message_callback=callback)
        channel.start_consuming()
    except Exception as e:
        logger.error(f"❌ Erreur du listener RabbitMQ : {e}")


# --- THREAD 2 : Le Scanner GitHub -> Kafka ---
def fetch_real_commits():
    logger.info(f"🦅 Le Scanner GitHub est en vol (Kafka: {KAFKA_BROKER})...")
    producer = Producer({'bootstrap.servers': KAFKA_BROKER})

    # Dictionnaire pour se souvenir du dernier commit vu pour chaque repo
    last_seen_timestamps = {}

    while True:
        with repos_lock:
            current_targets = list(target_repos)

        if not current_targets:
            logger.info("📡 Scanner en attente... Aucun projet GitDock enregistré.")
            time.sleep(10)
            continue

        for repo in current_targets:
            try:

                since = last_seen_timestamps.get(
                    repo,
                    (datetime.now(timezone.utc) - timedelta(hours=1)).isoformat()
                )

                api_url = f"https://api.github.com/repos/{repo}/commits"
                params = {"since": since, "per_page": 100}
                response = requests.get(api_url, headers=HEADERS, params=params)

                if response.status_code == 200:
                    commits = response.json()  # déjà du plus récent au plus ancien

                    if not commits:
                        continue

                    # On traite du plus ancien au plus récent (reverse)
                    for commit in reversed(commits):
                        commit_sha = commit["sha"]
                        author = commit.get("commit", {}).get("author", {}).get("name", "Unknown")

                        # Récupération du diff
                        diff_resp = requests.get(commit["url"], headers=DIFF_HEADERS)
                        if diff_resp.status_code != 200:
                            continue

                        data = {
                            "hash": commit_sha,
                            "project_id": repo,
                            "diff": diff_resp.text,
                            "author": author,
                            "source": "smart_fetcher"
                        }
                        producer.produce(TOPIC_NAME, value=json.dumps(data).encode('utf-8'))
                        logger.info(f"🚀 Commit {commit_sha[:7]} par {author} → Kafka")
                        time.sleep(1)  # rate-limit bienveillant

                    # Mise à jour du curseur temporel (ISO 8601)
                    newest_commit_date = commits[0]["commit"]["author"]["date"]
                    last_seen_timestamps[repo] = newest_commit_date

                elif response.status_code == 403:
                    reset_ts = int(response.headers.get("X-RateLimit-Reset", 0))
                    wait = max(0, reset_ts - int(time.time())) + 5
                    logger.warning(f"⏳ Rate limit GitHub. Pause de {wait}s...")
                    time.sleep(wait)

            except Exception as e:
                logger.error(f"⚠️ Erreur sur {repo} : {e}")

            producer.poll(0)
            time.sleep(60)  # Scan toutes les 30 secondes


if __name__ == "__main__":
    # Lancement des deux threads en parallèle
    threading.Thread(target=rabbitmq_listener, daemon=True).start()
    fetch_real_commits()