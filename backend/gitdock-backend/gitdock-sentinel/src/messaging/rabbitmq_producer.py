import pika
import json
import logging
from src.core.config import settings

logger = logging.getLogger(__name__)


class RabbitMQProducer:
    def __init__(self):
        self.connection = None
        self.channel = None

    def connect(self):
        try:
            credentials = pika.PlainCredentials(settings.rabbitmq_user, settings.rabbitmq_password)
            parameters = pika.ConnectionParameters(
                host=settings.rabbitmq_host,
                port=settings.rabbitmq_port,
                credentials=credentials
            )
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            self.channel.exchange_declare(exchange='gitdock.exchange', exchange_type='topic', durable=True)
            logger.info(f"🐰 Connecté au RabbitMQ distant : {settings.rabbitmq_host}")
        except Exception as e:
            logger.error(f"❌ Erreur RabbitMQ : {e}")

    def publish_audit(self, payload: dict):
        if not self.channel or self.connection.is_closed:
            self.connect()

            if not self.channel:
                logger.warning("🐰 RabbitMQ injoignable, audit non transmis (mais affiché sur le Radar).")
                return

        try:
            self.channel.basic_publish(
                exchange='gitdock.exchange',
                routing_key='sentinel.audit.result',
                body=json.dumps(payload),
                properties=pika.BasicProperties(content_type='application/json')
            )
            logger.info(f"📤 Audit poussé vers Windows (RabbitMQ) : {payload['commit_hash']}")
        except Exception as e:
            logger.error(f"⚠️ Impossible d'envoyer à RabbitMQ : {e}")

    def close(self):
        if self.connection and not self.connection.is_closed:
            self.connection.close()


rabbitmq_producer = RabbitMQProducer()