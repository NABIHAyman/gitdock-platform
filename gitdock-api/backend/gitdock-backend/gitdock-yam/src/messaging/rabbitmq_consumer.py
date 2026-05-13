import pika
import json
import logging
import threading
from src.core.config import settings
from src.rag.chroma_service import chroma_service

logger = logging.getLogger(__name__)

class YamRabbitMQConsumer:
    def __init__(self):
        self.connection = None
        self.channel = None
        self.queue_name = 'gitdock.yam.events.queue'

    def start(self):
        logger.info("🐰 Démarrage des oreilles de YAM sur RabbitMQ...")
        threading.Thread(target=self._consume, daemon=True).start()

    def _consume(self):
        try:
            credentials = pika.PlainCredentials(settings.rabbitmq_user, settings.rabbitmq_password)
            self.connection = pika.BlockingConnection(pika.ConnectionParameters(
                host=settings.rabbitmq_host, port=settings.rabbitmq_port, credentials=credentials))
            self.channel = self.connection.channel()

            # Queue exclusive à YAM
            self.channel.queue_declare(queue=self.queue_name, durable=True)

            # 📡 FRÉQUENCE 1 : On s'abonne à gitdock.exchange (pour les commits/projets)
            self.channel.queue_bind(exchange='gitdock.exchange', queue=self.queue_name, routing_key='project.commit.saved')
            self.channel.queue_bind(exchange='gitdock.exchange', queue=self.queue_name, routing_key='collaborator.added.event')

            # 📡 FRÉQUENCE 2 (VITAL) : On s'abonne à task.exchange (pour PHP)
            self.channel.queue_bind(exchange='task.exchange', queue=self.queue_name, routing_key='task.completed')

            def callback(ch, method, properties, body):
                try:
                    payload = json.loads(body)
                    routing_key = method.routing_key

                    if routing_key == "project.commit.saved":
                        dev_id = payload.get("authorUserId")
                        if dev_id:
                            info = f"A pushé un commit (Hash: {payload.get('hash')}) avec {payload.get('additions', 0)} additions."
                            chroma_service.append_to_developer_profile(str(dev_id), info)

                    elif routing_key == "task.completed":
                        # Payload envoyé par PHP : {"taskId": X, "userId": Y, "taskLevel": Z, "xpReward": W}
                        dev_id = payload.get("userId")
                        if dev_id:
                            info = f"A complété une tâche de niveau {payload.get('taskLevel', 'Inconnu')} (+{payload.get('xpReward', 0)} XP)."
                            chroma_service.append_to_developer_profile(str(dev_id), info)

                    elif routing_key == "collaborator.added.event":
                        dev_id = payload.get("userId")
                        if dev_id:
                            info = f"A rejoint le projet ID {payload.get('projectId')} en tant que {payload.get('role')}."
                            chroma_service.append_to_developer_profile(str(dev_id), info)

                except Exception as e:
                    logger.error(f"⚠️ Erreur parsing RabbitMQ dans YAM : {e}")
                finally:
                    # On confirme toujours la réception pour ne pas bloquer la file
                    ch.basic_ack(delivery_tag=method.delivery_tag)

            self.channel.basic_qos(prefetch_count=1)
            self.channel.basic_consume(queue=self.queue_name, on_message_callback=callback)
            self.channel.start_consuming()

        except Exception as e:
            logger.error(f"❌ Erreur RabbitMQ YAM : {e}")

yam_consumer = YamRabbitMQConsumer()