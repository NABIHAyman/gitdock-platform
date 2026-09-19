import json
import logging
import threading
import asyncio
from confluent_kafka import Consumer
from src.core.config import settings
from src.rag.chroma_service import chroma_service
from src.llm.sentinel_analyzer import sentinel_analyzer
from src.messaging.rabbitmq_producer import rabbitmq_producer
from src.messaging.websocket_manager import ws_manager


logger = logging.getLogger(__name__)

class SentinelConsumer:
    def __init__(self):
        self.bootstrap_servers = f"{settings.kafka_host}:{settings.kafka_port}"
        self.topic = 'cleaned-commits'
        self.consumer = None
        self._stop_event = threading.Event()

    def start(self):
        try:
            # 🟢 Configuration UNIQUE et Robuste
            conf = {
                'bootstrap.servers': self.bootstrap_servers,
                'group.id': 'gitdock-sentinel-group-v5', # 👈 Nouveau groupe pour ignorer le lag passé
                'auto.offset.reset': 'latest'          # earliest
            }
            self.consumer = Consumer(conf)
            self.consumer.subscribe(['cleaned-commits'])
            logger.info(f"🎧 Écoute sur Kafka : cleaned-commits (Mode: latest)")

            thread = threading.Thread(target=self._consume_loop, daemon=True)
            thread.start()
        except Exception as e:
            logger.error(f"❌ Erreur Kafka : {e}")

    def _consume_loop(self):
        rabbitmq_producer.connect()

        try:
            while not self._stop_event.is_set():
                msg = self.consumer.poll(1.0)
                if msg is None: continue
                if msg.error():
                    logger.error(f"⚠️ Erreur Kafka Poll: {msg.error()}")
                    continue

                try:
                    data = json.loads(msg.value().decode('utf-8'))
                except json.JSONDecodeError:
                    continue

                commit_hash = data.get("hash", "unknown")
                project_id = str(data.get("project_id", "0"))
                code_diff = data.get("diff", "")
                author = data.get("author", "Unknown")

                if code_diff:
                    # 1. Sauvegarde vectorielle
                    chroma_service.upsert_code_chunk(commit_hash, project_id, code_diff)

                    # 2. Audit IA (appel asynchrone dans un thread synchrone)
                    report = asyncio.run(sentinel_analyzer.analyze_commit(commit_hash, project_id, author, code_diff))

                    # 3. Formatage du résultat
                    payload = {
                        "type": "AUDIT_RESULT",
                        "commit_hash": commit_hash[:7],
                        "project_id": project_id,
                        "author": author,
                        "is_clean": report.is_clean,
                        "summary": report.summary,
                        "vulnerabilities": [v.model_dump() for v in report.vulnerabilities]
                    }

                    import src.main as main_module

                    # 4. 📡 WebSocket : Fix de Claude pour la Loop principale
                    if main_module.main_loop:
                        future = asyncio.run_coroutine_threadsafe(
                            ws_manager.broadcast_json(payload),
                            main_module.main_loop
                        )
                        try:
                            future.result(timeout=5)
                        except Exception as e:
                            logger.error(f"❌ Erreur envoi WebSocket: {e}")

                    # 5. Logs & RabbitMQ
                    if not report.is_clean:
                        logger.warning(f"🚨 FAILLE [{commit_hash[:7]}] : {report.summary}")

                    rabbitmq_producer.publish_audit(payload)

        except Exception as e:
            logger.error(f"❌ Crash boucle consommation: {e}")
        finally:
            # 🧹 Nettoyage propre
            rabbitmq_producer.close()
            if self.consumer:
                self.consumer.close()

    def stop(self):
        self._stop_event.set()

sentinel_consumer = SentinelConsumer()