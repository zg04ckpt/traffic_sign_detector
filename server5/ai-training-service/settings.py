import os
from dataclasses import dataclass
from pathlib import Path

@dataclass
class WorkerSettings:
    rabbitmq_host: str
    rabbitmq_port: int
    rabbitmq_user: str
    rabbitmq_password: str
    training_exchange: str
    request_queue: str
    status_queue: str
    request_routing_key: str
    status_routing_key: str
    runtime_dir: Path
    uploads_dir: Path
    model_store_dir: Path
    model_output_dir: Path
    model_artifact_public_prefix: str
    reconnect_delay_seconds: int

def _env(*keys: str, default: str = "") -> str:
    for key in keys:
        value = os.getenv(key)
        if value is not None and value.strip() != "":
            return value
    if not default:
        raise ValueError(f"Missing required environment variable for keys: {keys}")
    return default

def load_settings() -> WorkerSettings:
    return WorkerSettings(
        rabbitmq_host=_env("RABBITMQ_HOST", "SPRING_RABBITMQ_HOST", default="rabbitmq"),
        rabbitmq_port=int(_env("RABBITMQ_PORT", "SPRING_RABBITMQ_PORT", default="5672")),
        rabbitmq_user=_env("RABBITMQ_USERNAME", "SPRING_RABBITMQ_USERNAME", default="server5"),
        rabbitmq_password=_env("RABBITMQ_PASSWORD", "SPRING_RABBITMQ_PASSWORD", default="server5pass"),
        training_exchange=_env("TRAINING_EXCHANGE", "APP_RABBITMQ_EXCHANGE_TRAINING", default="training.exchange"),
        request_queue=_env("TRAINING_REQUEST_QUEUE", "APP_RABBITMQ_QUEUE_TRAINING_REQUEST", default="training.request.queue"),
        status_queue=_env("TRAINING_STATUS_QUEUE", "APP_RABBITMQ_QUEUE_TRAINING_STATUS", default="training.status.queue"),
        request_routing_key=_env("TRAINING_REQUEST_ROUTING_KEY", "APP_RABBITMQ_ROUTING_KEY_TRAINING_REQUEST", default="training.request"),
        status_routing_key=_env("TRAINING_STATUS_ROUTING_KEY", "APP_RABBITMQ_ROUTING_KEY_TRAINING_STATUS", default="training.status"),
        runtime_dir=Path(_env("TRAINING_RUNTIME_DIR", default="/app/runtime/training")).resolve(),
        uploads_dir=Path(_env("TRAINING_UPLOADS_DIR", "APP_STORAGE_UPLOAD_DIR", default="/data/uploads")).resolve(),
        model_store_dir=Path(_env("TRAINING_MODEL_STORE_DIR", "APP_TRAINING_ARTIFACT_AIMODEL_MODEL_DIR", default="/data/aimodel")).resolve(),
        model_output_dir=Path(_env("TRAINING_MODEL_OUTPUT_DIR", default="/data/runtime/training/outputs")).resolve(),
        model_artifact_public_prefix=_env("MODEL_ARTIFACT_PUBLIC_PREFIX", "TRAINING_MODEL_ARTIFACT_PUBLIC_PREFIX", default="/runtime/training/outputs"),
        reconnect_delay_seconds=int(_env("WORKER_RECONNECT_DELAY_SECONDS", default="5")),
    )
