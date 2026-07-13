import json
import logging
from datetime import datetime, timezone
from typing import Optional

import pika

from settings import WorkerSettings

LOGGER = logging.getLogger("rabbitmq-listener")

class RabbitMQListener:
    def __init__(self, settings: WorkerSettings):
        self.settings = settings

    def connect_and_listen(self, on_message_callback):
        connection_params = pika.ConnectionParameters(
            host=self.settings.rabbitmq_host,
            port=self.settings.rabbitmq_port,
            credentials=pika.PlainCredentials(self.settings.rabbitmq_user, self.settings.rabbitmq_password),
            heartbeat=30,
            blocked_connection_timeout=300,
        )

        LOGGER.info(
            "Connecting RabbitMQ host=%s port=%s queue=%s",
            self.settings.rabbitmq_host,
            self.settings.rabbitmq_port,
            self.settings.request_queue,
        )

        connection = pika.BlockingConnection(connection_params)
        try:
            channel = connection.channel()
            self._declare_topology(channel)
            channel.basic_qos(prefetch_count=1)
            
            # Wrapper to handle ack
            def callback_wrapper(ch, method, properties, body):
                try:
                    on_message_callback(ch, method, properties, body)
                finally:
                    ch.basic_ack(delivery_tag=method.delivery_tag)
                    
            channel.basic_consume(queue=self.settings.request_queue, on_message_callback=callback_wrapper, auto_ack=False)
            LOGGER.info("Waiting for training jobs...")
            channel.start_consuming()
        finally:
            if connection.is_open:
                connection.close()

    def _declare_topology(self, channel: pika.adapters.blocking_connection.BlockingChannel) -> None:
        channel.exchange_declare(exchange=self.settings.training_exchange, exchange_type="direct", durable=True)
        channel.queue_declare(queue=self.settings.request_queue, durable=True)
        channel.queue_declare(queue=self.settings.status_queue, durable=True)
        channel.queue_bind(
            exchange=self.settings.training_exchange,
            queue=self.settings.request_queue,
            routing_key=self.settings.request_routing_key,
        )
        channel.queue_bind(
            exchange=self.settings.training_exchange,
            queue=self.settings.status_queue,
            routing_key=self.settings.status_routing_key,
        )

    def publish_status(
        self,
        channel,
        tracking_id: str,
        state: str,
        detail: str,
        current_epoch: Optional[int] = None,
        precision: Optional[float] = None,
        recall: Optional[float] = None,
        log_line: Optional[str] = None,
        model_artifact_path: Optional[str] = None,
    ) -> None:
        status_message = {
            "trackingId": tracking_id,
            "state": state,
            "detail": detail,
            "updatedAt": datetime.now(timezone.utc).isoformat(),
            "currentEpoch": current_epoch,
            "precision": precision,
            "recall": recall,
            "logLine": log_line,
            "modelArtifactPath": model_artifact_path,
        }

        channel.basic_publish(
            exchange=self.settings.training_exchange,
            routing_key=self.settings.status_routing_key,
            body=json.dumps(status_message),
            properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        )
