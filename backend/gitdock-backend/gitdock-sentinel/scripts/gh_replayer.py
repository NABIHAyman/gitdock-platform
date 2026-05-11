import json
import gzip
import time
import logging
from kafka import KafkaProducer

# ==========================================
# ⚙️ CONFIGURATION
# ==========================================
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

KAFKA_BROKER = "ubuntu-host:9092"
TOPIC_NAME = "raw-commits"  # ⚠️ ATTENTION : C'est le flux brut (Zone 1)

# Le fichier GH Archive décompressé (ou même le .gz direct)
GH_ARCHIVE_FILE = "../../data/2026-04-01-15.json.gz"  # Remplace par ton fichier


# ==========================================
# 🚀 LE MOTEUR D'INJECTION (REPLAYER)
# ==========================================
def run_replayer():
    logger.info(f"💿 Démarrage du Replayer GH Archive vers {KAFKA_BROKER}...")

    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKER,
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            retries=3
        )
    except Exception as e:
        logger.error(f"❌ Impossible de se connecter à Kafka : {e}")
        return

    logger.info("✅ Connecté ! Lecture du fichier...")

    count = 0

    # On lit le fichier .gz ligne par ligne pour ne pas exploser la RAM
    try:
        with gzip.open(GH_ARCHIVE_FILE, 'rt', encoding='utf-8') as f:
            for line in f:
                event = json.loads(line)

                # On ne garde QUE les événements de type "PushEvent" (commits)
                if event.get("type") == "PushEvent":
                    payload = event.get("payload", {})
                    commits = payload.get("commits", [])
                    repo_name = event.get("repo", {}).get("name", "unknown")

                    for commit in commits:
                        # Structure adaptée pour ton Kafka Consumer
                        kafka_message = {
                            "hash": commit.get("sha"),
                            "project_id": repo_name,  # Le nom du repo devient le tenant
                            "diff": commit.get("message", ""),
                            # GH Archive ne donne pas le code source, mais le message long
                            "author": commit.get("author", {}).get("name", "Unknown"),
                            "source": "gh_archive"
                        }

                        producer.send(TOPIC_NAME, value=kafka_message)
                        count += 1

                        if count % 100 == 0:
                            logger.info(f"🌊 {count} vrais commits injectés...")

                        # Vitesse de simulation (ex: 50 commits/sec)
                        time.sleep(0.02)

    except KeyboardInterrupt:
        logger.info("\n🛑 Arrêt manuel du Replayer.")
    except Exception as e:
        logger.error(f"❌ Erreur de lecture : {e}")
    finally:
        producer.flush()
        producer.close()
        logger.info(f"🏁 Terminé. {count} vrais commits injectés dans le pipeline.")


if __name__ == "__main__":
    run_replayer()