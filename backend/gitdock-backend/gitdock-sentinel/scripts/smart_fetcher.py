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


# Charge le .env situé dans le dossier parent
load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), '..', '.env'))

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

# --- CONFIGURATION KAFKA (Local Ubuntu) ---
KAFKA_BROKER = f"{os.getenv('KAFKA_HOST', '127.0.0.1')}:{os.getenv('KAFKA_PORT', 9092)}"
TOPIC_NAME = "raw-commits"

# --- CONFIGURATION RABBITMQ (Distant Windows via Tailscale) ---
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "windows-host")  # L'IP de ton PC
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", 5672))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")

GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")

if not GITHUB_TOKEN:
    logger.error("❌ DANGER : Aucun GITHUB_TOKEN trouvé. Limite de 60 requêtes/heure !")
    exit(1)

HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "X-GitHub-Api-Version": "2026-03-10"
}
DIFF_HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3.diff",
    "X-GitHub-Api-Version": "2026-03-10"
}

# 🎯 La liste dynamique des cibles
target_repos = set()
repos_lock = threading.Lock()


# =========================================================
# 🧵 THREAD 1 : LES GRANDES OREILLES (RabbitMQ Listener)
# =========================================================
def rabbitmq_listener():
    logger.info(f"🐰 Écoute RabbitMQ activée sur {RABBITMQ_HOST}...")
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        connection = pika.BlockingConnection(pika.ConnectionParameters(
            host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=credentials))
        channel = connection.channel()

        # ✅ Queue EXCLUSIVE à sentinel — ne PAS partager avec gitdock-sync
        sentinel_queue = 'gitdock.sentinel.targets.queue'
        channel.queue_declare(queue=sentinel_queue, durable=True)

        # ✅ Binding sur le même exchange/routing key → reçoit une COPIE du message
        # gitdock-sync garde sa propre queue 'gitdock.sync.request.queue' intacte
        channel.queue_bind(
            exchange='gitdock.exchange',
            queue=sentinel_queue,
            routing_key='sync.request'
        )

        def callback(ch, method, properties, body):
            try:
                data = json.loads(body)
            except Exception:
                ch.basic_ack(delivery_tag=method.delivery_tag)
                return

            repo_url = data.get("repoUrl", "")
            if "github.com" in repo_url:
                parts = repo_url.rstrip('/').split('/')
                if len(parts) >= 2:
                    repo_path = f"{parts[-2]}/{parts[-1]}"
                    with repos_lock:
                        target_repos.add(repo_path)
                    logger.info(f"🎯 NOUVELLE CIBLE VERROUILLÉE : {repo_path}")

            ch.basic_ack(delivery_tag=method.delivery_tag)

        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(queue=sentinel_queue, on_message_callback=callback)
        channel.start_consuming()

    except Exception as e:
        logger.error(f"❌ Erreur du listener RabbitMQ : {e}")


# =========================================================
# 🦅 THREAD 2 : LE TIREUR D'ÉLITE (GitHub -> Kafka)
# =========================================================
def fetch_real_commits():
    logger.info(f"🦅 Le Scanner GitHub est en vol (Cible Kafka: {KAFKA_BROKER})...")

    try:
        producer = Producer({'bootstrap.servers': KAFKA_BROKER})
    except Exception as e:
        logger.error(f"❌ Kafka introuvable : {e}")
        return

    last_seen_timestamps = {}

    while True:
        with repos_lock:
            current_targets = list(target_repos)

        if not current_targets:
            logger.info("📡 Scanner en attente... Aucun projet GitHub dans le viseur.")
            time.sleep(10)
            continue

        for repo in current_targets:
            try:
                # Fenêtre initiale : dernière heure si première fois
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
        time.sleep(60)  # On boucle toutes les 30s


if __name__ == "__main__":
    # On lance les deux threads en parallèle
    threading.Thread(target=rabbitmq_listener, daemon=True).start()
    fetch_real_commits()