import json
import time
import random
import logging
from confluent_kafka import Producer

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

# --- CONFIGURATION ---
KAFKA_BROKER = "ubuntu-host:9092"
TOPIC_NAME = "cleaned-commits"

# Scénario de test : 2 propres, 2 avec secrets
COMMITS_TO_SEND = [
    {"diff": "def calculate_total(items):\n    return sum(item.price for item in items)", "dirty": False},
    {
        "diff": "const AWS_CONFIG = {\n  accessKeyId: 'AKIAIOSFODNN7EXAMPLE',\n  secretAccessKey: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'\n};",
        "dirty": True},
    {"diff": "// Refactoring du composant Button\nconst Button = ({ label }) => <button>{label}</button>;",
     "dirty": False},
    {
        "diff": "def connect_db():\n    db_password = 'SuperSecretPassword123!'\n    conn = psycopg2.connect(password=db_password)",
        "dirty": True}
]


def run_chaos_monkey_lite():
    logger.info(f"🐒 Démarrage du Chaos Monkey LITE (Séquence de 4 commits)...")

    try:
        producer = Producer({'bootstrap.servers': KAFKA_BROKER})
    except Exception as e:
        logger.error(f"❌ Impossible de se connecter à Kafka : {e}")
        return

    time.sleep(2)

    for i, scenario in enumerate(COMMITS_TO_SEND):
        commit_hash = f"{random.getrandbits(32):08x}"
        project_id = str(random.randint(1, 5))

        payload = {
            "hash": commit_hash,
            "project_id": f"demo-repo-{project_id}",
            "diff": scenario["diff"],
            "author": "ChaosMonkey-Lite",
            "source": "chaos_monkey_lite"
        }

        # Injection Kafka
        producer.produce(TOPIC_NAME, value=json.dumps(payload).encode('utf-8'))
        producer.poll(0)

        icon = "🚨" if scenario["dirty"] else "✅"
        logger.info(f"{icon} [#{i + 1}/4] Envoi du commit {commit_hash[:7]} (Dirty: {scenario['dirty']})")

        # Pause de 3 secondes entre chaque commit pour laisser le temps à l'IA d'analyser
        # et au Radar de mettre à jour l'interface Web
        time.sleep(3)

    producer.flush()
    logger.info(f"🏁 Mission terminée. 4 commits de test injectés. Sentinel va prendre le relais.")


if __name__ == "__main__":
    run_chaos_monkey_lite()