from __future__ import annotations

import json
import logging
import os
import random
import re
import shutil
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse

import pika
import yaml

from train_traffic_signs import TrainingConfig, train_model


LOGGER = logging.getLogger("training-python-worker")


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


@dataclass
class PreparedSample:
    source_image: Path
    image_name: str
    labels: List[str]


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


class TrainingWorker:
    def __init__(self, settings: WorkerSettings):
        self.settings = settings
        self.base_dir = Path(__file__).resolve().parent

    def run_forever(self) -> None:
        while True:
            try:
                self._run_once()
            except KeyboardInterrupt:
                LOGGER.info("Worker interrupted, shutting down")
                return
            except Exception as exc:
                LOGGER.exception("Worker loop failed: %s", exc)
                time.sleep(self.settings.reconnect_delay_seconds)

    def _run_once(self) -> None:
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
            channel.basic_consume(queue=self.settings.request_queue, on_message_callback=self._handle_message, auto_ack=False)
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

    def _handle_message(self, channel, method, _properties, body: bytes) -> None:
        payload_text = body.decode("utf-8", errors="replace")
        payload = _load_json(payload_text)
        tracking_id = str(_case_insensitive_get(payload, "trackingId") or "")

        try:
            if not tracking_id:
                raise ValueError("Training message missing trackingId")

            LOGGER.info("Received training job trackingId=%s", tracking_id)
            self._process_job(payload, tracking_id, channel)
            LOGGER.info("Training job completed trackingId=%s", tracking_id)
        except Exception as exc:
            LOGGER.exception("Training job failed trackingId=%s error=%s", tracking_id, exc)
            if tracking_id:
                self._publish_status(
                    channel,
                    tracking_id=tracking_id,
                    state="FAILED",
                    detail=str(exc),
                )
        finally:
            channel.basic_ack(delivery_tag=method.delivery_tag)

    def _process_job(self, payload: Dict[str, Any], tracking_id: str, channel) -> None:
        self._publish_status(
            channel,
            tracking_id=tracking_id,
            state="PREPARING_DATASET",
            detail="Preparing dataset for training",
        )

        samples = _normalize_list_of_maps(_case_insensitive_get(payload, "samples"))
        self._setup_dataset(samples)

        self._publish_status(
            channel,
            tracking_id=tracking_id,
            state="STARTED",
            detail="Training process started",
        )

        model_path = self._resolve_model_path(_as_string(_case_insensitive_get(payload, "modelPath"), default="/models/yolov8s.pt"))
        config = TrainingConfig(
            model_path=model_path,
            data_yaml_path=self.settings.runtime_dir / "traffic_signs.yaml",
            project_dir=self.settings.runtime_dir / "runs" / "detect" / "runs" / "train",
            run_name=f"traffic_signs_{_safe_file_name(tracking_id)}",
            epochs=_as_int(_case_insensitive_get(payload, "epochs"), default=20),
            batch_size=_as_int(_case_insensitive_get(payload, "batchSize"), default=32),
            learning_rate=_as_float(_case_insensitive_get(payload, "learningRate"), default=0.01),
            image_size=_as_int(_case_insensitive_get(payload, "imageSize"), default=416),
            patience=_as_int(_case_insensitive_get(payload, "earlyStoppingPatience"), default=5),
            device=_as_string(_case_insensitive_get(payload, "device"), default="cpu"),
            optimizer=_as_string(_case_insensitive_get(payload, "optimizer"), default="Adam"),
        )

        result = train_model(
            config,
            on_progress=lambda detail, epoch, precision, recall: self._publish_status(
                channel,
                tracking_id=tracking_id,
                state="RUNNING",
                detail=detail,
                current_epoch=epoch,
                precision=precision,
                recall=recall,
                log_line=detail,
            ),
        )

        model_artifact_path = self._export_model_artifact(result.best_model_path, tracking_id)

        self._publish_status(
            channel,
            tracking_id=tracking_id,
            state="COMPLETED",
            detail="Training finished successfully",
            current_epoch=result.current_epoch,
            precision=result.precision,
            recall=result.recall,
            model_artifact_path=model_artifact_path,
        )

    def _setup_dataset(self, raw_samples: List[Dict[str, Any]]) -> None:
        samples = self._build_samples(raw_samples)
        if not samples:
            raise RuntimeError("No samples in training payload")

        images_root = self.settings.runtime_dir / "dataset" / "images"
        labels_root = self.settings.runtime_dir / "dataset" / "labels"

        for split in ("train", "test", "val"):
            _reset_dir(images_root / split)
            _reset_dir(labels_root / split)

        random.Random(42).shuffle(samples)
        train_count = int(len(samples) * 0.8)
        test_count = int(len(samples) * 0.1)

        for index, sample in enumerate(samples):
            if index < train_count:
                split = "train"
            elif index < train_count + test_count:
                split = "test"
            else:
                split = "val"
            self._write_sample(sample, split, images_root, labels_root)

        self._write_dataset_yaml(self.settings.runtime_dir / "traffic_signs.yaml")

    def _build_samples(self, payload_samples: List[Dict[str, Any]]) -> List[PreparedSample]:
        samples: List[PreparedSample] = []

        for item in payload_samples:
            info = _as_map(_case_insensitive_get(item, "ThongTin"))
            if not info:
                info = item

            image_ref = _as_string(_case_insensitive_get(info, "DuongDanAnh"), default="").strip()
            if not image_ref:
                continue

            image_name = _extract_file_name(image_ref)
            source_image = self._resolve_image_path(image_ref, image_name)

            labels: List[str] = []
            boxes = _normalize_list_of_maps(_case_insensitive_get(info, "DsBien"))
            for box in boxes:
                sign = _as_map(_case_insensitive_get(box, "Bien"))
                class_id = _as_int(_case_insensitive_get(sign, "Id"), default=0)
                x_center = _as_float(_case_insensitive_get(box, "XCenter"), default=0.0)
                y_center = _as_float(_case_insensitive_get(box, "YCenter"), default=0.0)
                width = _as_float(_case_insensitive_get(box, "W"), default=0.0)
                height = _as_float(_case_insensitive_get(box, "H"), default=0.0)
                labels.append(
                    f"{class_id} {self._format_decimal(x_center)} {self._format_decimal(y_center)} "
                    f"{self._format_decimal(width)} {self._format_decimal(height)}"
                )

            samples.append(PreparedSample(source_image=source_image, image_name=image_name, labels=labels))

        return samples

    def _write_sample(self, sample: PreparedSample, split: str, images_root: Path, labels_root: Path) -> None:
        target_image = images_root / split / sample.image_name
        shutil.copy2(sample.source_image, target_image)

        image_stem = Path(sample.image_name).stem
        target_label = labels_root / split / f"{image_stem}.txt"
        target_label.write_text("\n".join(sample.labels), encoding="utf-8")

    def _write_dataset_yaml(self, target_yaml_path: Path) -> None:
        template_yaml = self.base_dir / "traffic_signs.yaml"
        names: List[str] = []

        if template_yaml.exists():
            parsed = yaml.safe_load(template_yaml.read_text(encoding="utf-8")) or {}
            raw_names = parsed.get("names")
            if isinstance(raw_names, dict):
                names = [str(raw_names[key]) for key in sorted(raw_names.keys(), key=lambda x: int(x))]
            elif isinstance(raw_names, list):
                names = [str(item) for item in raw_names]

        if not names:
            names = ["other"]

        yaml_payload = {
            "path": str((self.settings.runtime_dir / "dataset").resolve()),
            "train": "images/train",
            "val": "images/val",
            "test": "images/test",
            "nc": len(names),
            "names": names,
        }

        target_yaml_path.parent.mkdir(parents=True, exist_ok=True)
        target_yaml_path.write_text(yaml.safe_dump(yaml_payload, sort_keys=False), encoding="utf-8")

    def _resolve_image_path(self, image_ref: str, image_name: str) -> Path:
        absolute = _to_absolute_path(image_ref)
        if absolute and absolute.exists():
            return absolute

        normalized = image_ref.replace("\\", "/")

        if normalized.startswith("http://") or normalized.startswith("https://"):
            uri_path = urlparse(normalized).path
            if uri_path.startswith("/uploads/"):
                candidate = self.settings.uploads_dir / uri_path[len("/uploads/") :]
                if candidate.exists():
                    return candidate

        if normalized.startswith("/uploads/"):
            candidate = self.settings.uploads_dir / normalized[len("/uploads/") :]
            if candidate.exists():
                return candidate

        if normalized.startswith("uploads/"):
            candidate = Path.cwd() / normalized
            if candidate.exists():
                return candidate

        uploads_candidate = self.settings.uploads_dir / image_name
        if uploads_candidate.exists():
            return uploads_candidate

        relative_candidate = (Path.cwd() / image_ref).resolve()
        if relative_candidate.exists():
            return relative_candidate

        raise FileNotFoundError(f"Image file not found for sample: {image_ref}")

    def _resolve_model_path(self, model_ref: str) -> str:
        value = (model_ref or "").strip()
        if not value:
            value = "/models/yolov8s.pt"

        absolute = _to_absolute_path(value)
        if absolute and absolute.exists():
            return str(absolute)

        normalized = value.replace("\\", "/")

        if normalized.startswith("/models/"):
            candidate = self.settings.model_store_dir / normalized[len("/models/") :]
            if candidate.exists():
                return str(candidate)

        if normalized.startswith("models/"):
            candidate = (Path.cwd() / normalized).resolve()
            if candidate.exists():
                return str(candidate)

        runtime_candidate = (self.settings.runtime_dir / normalized).resolve()
        if runtime_candidate.exists():
            return str(runtime_candidate)

        base_candidate = (self.base_dir / normalized).resolve()
        if base_candidate.exists():
            return str(base_candidate)

        runtime_fallback = self.settings.runtime_dir / "yolov8s.pt"
        if runtime_fallback.exists():
            return str(runtime_fallback)

        base_fallback = self.base_dir / "yolov8s.pt"
        if base_fallback.exists():
            return str(base_fallback)

        return "yolov8s.pt"

    def _export_model_artifact(self, best_model_path: Path, tracking_id: str) -> str:
        self.settings.model_output_dir.mkdir(parents=True, exist_ok=True)
        destination_file_name = f"training-{_safe_file_name(tracking_id)}.pt"
        destination = self.settings.model_output_dir / destination_file_name
        shutil.copy2(best_model_path, destination)

        prefix = self.settings.model_artifact_public_prefix.rstrip("/")
        return f"{prefix}/{destination_file_name}"

    def _publish_status(
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

    def _format_decimal(self, value: float) -> str:
        return f"{value:.6f}".rstrip("0").rstrip(".") if "." in f"{value:.6f}" else f"{value:.6f}"


def _load_json(payload: str) -> Dict[str, Any]:
    try:
        parsed = json.loads(payload)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON message: {exc}") from exc

    if not isinstance(parsed, dict):
        raise ValueError("Training payload must be a JSON object")
    return parsed


def _env(*keys: str, default: str = "") -> str:
    for key in keys:
        value = os.getenv(key)
        if value is not None and value.strip() != "":
            return value
    return default


def _case_insensitive_get(source: Dict[str, Any], key: str) -> Any:
    if key in source:
        return source[key]

    lowered = key.lower()
    for existing_key, value in source.items():
        if isinstance(existing_key, str) and existing_key.lower() == lowered:
            return value
    return None


def _as_map(value: Any) -> Dict[str, Any]:
    if isinstance(value, dict):
        return value
    return {}


def _normalize_list_of_maps(value: Any) -> List[Dict[str, Any]]:
    if not isinstance(value, list):
        return []

    result: List[Dict[str, Any]] = []
    for item in value:
        if isinstance(item, dict):
            result.append(item)
    return result


def _to_absolute_path(raw_path: str) -> Optional[Path]:
    try:
        path = Path(raw_path)
    except Exception:
        return None

    if path.is_absolute():
        return path.resolve()
    return None


def _extract_file_name(path_value: str) -> str:
    normalized = path_value.replace("\\", "/")
    without_query = normalized.split("?", 1)[0].split("#", 1)[0]
    return without_query.rsplit("/", 1)[-1]


def _safe_file_name(value: str) -> str:
    safe = re.sub(r"[^A-Za-z0-9._-]", "_", value)
    return safe or "unknown"


def _reset_dir(path: Path) -> None:
    if path.exists():
        for child in sorted(path.rglob("*"), reverse=True):
            if child.is_file() or child.is_symlink():
                child.unlink(missing_ok=True)
            elif child.is_dir():
                child.rmdir()
    path.mkdir(parents=True, exist_ok=True)


def _as_int(value: Any, default: int) -> int:
    if value is None:
        return default
    if isinstance(value, bool):
        return int(value)
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def _as_float(value: Any, default: float) -> float:
    if value is None:
        return default
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def _as_string(value: Any, default: str) -> str:
    if value is None:
        return default
    text = str(value).strip()
    return text or default


def main() -> None:
    logging.basicConfig(
        level=os.getenv("LOG_LEVEL", "INFO").upper(),
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    settings = load_settings()
    settings.runtime_dir.mkdir(parents=True, exist_ok=True)
    settings.model_output_dir.mkdir(parents=True, exist_ok=True)
    worker = TrainingWorker(settings)
    worker.run_forever()


if __name__ == "__main__":
    main()
