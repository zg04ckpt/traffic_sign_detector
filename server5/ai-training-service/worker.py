import json
import logging
import os
import time
from typing import Any, Dict, List

from settings import load_settings, WorkerSettings
from rabbitmq_listener import RabbitMQListener
from dataset_manager import DatasetManager
from trainer_engine import TrainerEngine
from utils import case_insensitive_get, normalize_list_of_maps

LOGGER = logging.getLogger("training-python-worker")

class TrainingWorker:
    def __init__(self, settings: WorkerSettings):
        self.settings = settings
        self.listener = RabbitMQListener(settings)
        self.dataset_manager = DatasetManager(settings)
        self.trainer_engine = TrainerEngine(settings)

    def run_forever(self) -> None:
        while True:
            try:
                self.listener.connect_and_listen(self._handle_message)
            except KeyboardInterrupt:
                LOGGER.info("Worker interrupted, shutting down")
                return
            except Exception as exc:
                LOGGER.exception("Worker loop failed: %s", exc)
                time.sleep(self.settings.reconnect_delay_seconds)

    def _handle_message(self, channel, method, _properties, body: bytes) -> None:
        payload_text = body.decode("utf-8", errors="replace")
        payload = self._load_json(payload_text)
        tracking_id = str(case_insensitive_get(payload, "trackingId") or "")

        try:
            if not tracking_id:
                raise ValueError("Training message missing trackingId")

            LOGGER.info("Received training job trackingId=%s", tracking_id)
            self._process_job(payload, tracking_id, channel)
            LOGGER.info("Training job completed trackingId=%s", tracking_id)
        except Exception as exc:
            LOGGER.exception("Training job failed trackingId=%s error=%s", tracking_id, exc)
            if tracking_id:
                self.listener.publish_status(
                    channel,
                    tracking_id=tracking_id,
                    state="FAILED",
                    detail=str(exc),
                )

    def _process_job(self, payload: Dict[str, Any], tracking_id: str, channel) -> None:
        self.listener.publish_status(
            channel,
            tracking_id=tracking_id,
            state="PREPARING_DATASET",
            detail="Preparing dataset for training",
        )

        samples = self._extract_selected_samples(payload)
        self.dataset_manager.setup_dataset(samples)

        self.listener.publish_status(
            channel,
            tracking_id=tracking_id,
            state="STARTED",
            detail="Training process started",
        )

        def progress_callback(detail, epoch, precision, recall):
            self.listener.publish_status(
                channel,
                tracking_id=tracking_id,
                state="RUNNING",
                detail=detail,
                current_epoch=epoch,
                precision=precision,
                recall=recall,
                log_line=detail,
            )

        model_artifact_path = self.trainer_engine.run_training(tracking_id, payload, progress_callback)

        self.listener.publish_status(
            channel,
            tracking_id=tracking_id,
            state="COMPLETED",
            detail="Training finished successfully",
            model_artifact_path=model_artifact_path,
        )

    def _extract_selected_samples(self, payload: Dict[str, Any]) -> List[Dict[str, Any]]:
        selected = normalize_list_of_maps(case_insensitive_get(payload, "DsMauHL"))
        if selected:
            return selected

        selected = normalize_list_of_maps(case_insensitive_get(payload, "selectedSamples"))
        if selected:
            return selected

        selected = normalize_list_of_maps(case_insensitive_get(payload, "samples"))
        if selected:
            return selected

        raise ValueError("No selected samples found in training payload")

    def _load_json(self, payload: str) -> Dict[str, Any]:
        try:
            parsed = json.loads(payload)
        except json.JSONDecodeError as exc:
            raise ValueError(f"Invalid JSON message: {exc}") from exc

        if not isinstance(parsed, dict):
            raise ValueError("Training payload must be a JSON object")
        return parsed

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
