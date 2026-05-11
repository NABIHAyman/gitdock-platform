import json
import time
import random
import logging
from confluent_kafka import Producer
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

KAFKA_BROKER = "ubuntu-host:9092"
TOPIC_NAME = "cleaned-commits"

CLEAN_COMMITS = [
    "def calculate_total(items):\n    return sum(item.price for item in items)",
    "class User:\n    def __init__(self, name):\n        self.name = name",
    "// Refactoring du composant Button\nconst Button = ({ label }) => <button>{label}</button>;",
    "/* Mise à jour de la doc */\n# Installation\n`npm install`"
]

DIRTY_COMMITS = [
    "def connect_db():\n    db_password = 'SuperSecretPassword123!'\n    conn = psycopg2.connect(password=db_password)",
    "const AWS_CONFIG = {\n  accessKeyId: 'AKIAIOSFODNN7EXAMPLE',\n  secretAccessKey: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'\n};",
    "def get_user(user_id):\n    query = 'SELECT * FROM users WHERE id = ' + str(user_id)\n    return db.execute(query)",
    "function processQueue() {\n  let running = true;\n  while(running) {\n    // Pas de condition de sortie claire\n  }\n}"
]


def generate_random_commit():
    is_dirty = random.random() < 0.20
    code_diff = random.choice(DIRTY_COMMITS) if is_dirty else random.choice(CLEAN_COMMITS)
    commit_hash = f"{random.getrandbits(32):08x}"
    project_id = str(random.randint(1, 5))

    return {
        "hash": commit_hash,
        "project_id": f"simulated-repo-{project_id}",
        "diff": code_diff,
        "author": "ChaosMonkey",
        "source": "chaos_monkey"
    }


def run_chaos_monkey():
    logger.info(f"🐒 Démarrage du Chaos Monkey vers {KAFKA_BROKER}...")

    try:
        producer = Producer({'bootstrap.servers': KAFKA_BROKER})
    except Exception as e:
        logger.error(f"❌ Impossible de se connecter à Kafka : {e}")
        return

    logger.info("✅ Connecté ! Inondation dans 3 secondes... (Ctrl+C pour arrêter)")
    time.sleep(3)

    count = 0
    try:
        while True:
            payload = generate_random_commit()

            # Injection via confluent_kafka
            producer.produce(TOPIC_NAME, value=json.dumps(payload).encode('utf-8'))
            producer.poll(0)

            count += 1
            icon = "🚨" if payload["source"] == "chaos_monkey" and payload["diff"] in DIRTY_COMMITS else "✅"
            logger.info(f"{icon} [#{count}] Envoi du commit {payload['hash']} (Projet {payload['project_id']})")

            time.sleep(random.uniform(0.5, 2.0))

    except KeyboardInterrupt:
        logger.info("\n🛑 Arrêt manuel du Chaos Monkey.")
    finally:
        producer.flush()
        logger.info(f"🏁 Terminé. {count} faux commits injectés dans le pipeline.")


if __name__ == "__main__":
    run_chaos_monkey()