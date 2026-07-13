import shutil
from pathlib import Path

from train_traffic_signs import TrainingConfig, train_model
from settings import WorkerSettings
from utils import safe_file_name, to_absolute_path
from minio_client import MinioClientWrapper

class TrainerEngine:
    def __init__(self, settings: WorkerSettings, minio_client: MinioClientWrapper):
        self.settings = settings
        self.minio_client = minio_client
        self.base_dir = Path(__file__).resolve().parent

    def run_training(self, tracking_id: str, payload_config: dict, progress_callback) -> str:
        model_path = self._resolve_model_path(payload_config.get("modelPath"))
        
        config = TrainingConfig(
            model_path=model_path,
            data_yaml_path=self.settings.runtime_dir / "traffic_signs.yaml",
            project_dir=self.settings.runtime_dir / "runs" / "detect" / "runs" / "train",
            run_name=f"traffic_signs_{safe_file_name(tracking_id)}",
            epochs=payload_config["epochs"],
            batch_size=payload_config["batchSize"],
            learning_rate=payload_config["learningRate"],
            image_size=payload_config["imageSize"],
            patience=payload_config["earlyStoppingPatience"],
            device=payload_config["device"],
            optimizer=payload_config["optimizer"],
        )

        result = train_model(
            config,
            on_progress=progress_callback
        )

        return self._export_model_artifact(result.best_model_path, tracking_id)

    def _resolve_model_path(self, model_ref: str) -> str:
        value = (model_ref or "").strip()
        if not value:
            raise ValueError("Model path is missing")

        absolute = to_absolute_path(value)
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

        raise FileNotFoundError(f"Base model not found: {model_ref}")

    def _export_model_artifact(self, best_model_path: Path, tracking_id: str) -> str:
        self.settings.model_output_dir.mkdir(parents=True, exist_ok=True)
        destination_file_name = f"training-{safe_file_name(tracking_id)}.pt"
        destination = self.settings.model_output_dir / destination_file_name
        shutil.copy2(best_model_path, destination)

        # Upload to MinIO
        minio_path = self.minio_client.upload_model(destination, destination_file_name)
        if minio_path:
            return minio_path

        prefix = self.settings.model_artifact_public_prefix.rstrip("/")
        return f"{prefix}/{destination_file_name}"
