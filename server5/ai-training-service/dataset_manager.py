import random
import shutil
import yaml
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List
from urllib.parse import urlparse

from settings import WorkerSettings
from utils import (
    as_int, as_float, as_string, case_insensitive_get, as_map,
    normalize_list_of_maps, to_absolute_path, extract_file_name, reset_dir
)
from minio_client import MinioClientWrapper

@dataclass
class PreparedSample:
    source_image: Path
    image_name: str
    labels: List[str]

class DatasetManager:
    def __init__(self, settings: WorkerSettings, minio_client: MinioClientWrapper):
        self.settings = settings
        self.minio_client = minio_client
        self.base_dir = Path(__file__).resolve().parent

    def setup_dataset(self, raw_samples: List[Dict[str, Any]]) -> None:
        samples = self._build_samples(raw_samples)
        if not samples:
            raise ValueError("No valid samples in training payload")

        images_root = self.settings.runtime_dir / "dataset" / "images"
        labels_root = self.settings.runtime_dir / "dataset" / "labels"

        for split in ("train", "test", "val"):
            reset_dir(images_root / split)
            reset_dir(labels_root / split)

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
            info = as_map(case_insensitive_get(item, "ThongTin"))
            if not info:
                info = item

            image_ref = as_string(case_insensitive_get(info, "DuongDanAnh"), "DuongDanAnh")
            image_name = extract_file_name(image_ref)
            source_image = self._resolve_image_path(image_ref, image_name)

            labels: List[str] = []
            boxes = normalize_list_of_maps(case_insensitive_get(info, "DsBien"))
            for box in boxes:
                sign = as_map(case_insensitive_get(box, "Bien"))
                class_id = as_int(case_insensitive_get(sign, "Id"), "Id")
                x_center = as_float(case_insensitive_get(box, "XCenter"), "XCenter")
                y_center = as_float(case_insensitive_get(box, "YCenter"), "YCenter")
                width = as_float(case_insensitive_get(box, "W"), "W")
                height = as_float(case_insensitive_get(box, "H"), "H")
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
        normalized = image_ref.replace("\\", "/")
        
        # Check if it's a MinIO path like /datasets/datasetName/uuid.jpg
        minio_prefix = f"/{self.settings.minio_datasets_bucket}/"
        if normalized.startswith(minio_prefix):
            object_name = normalized[len(minio_prefix):]
            downloads_dir = self.settings.runtime_dir / "raw_downloads"
            downloads_dir.mkdir(parents=True, exist_ok=True)
            download_path = downloads_dir / image_name
            
            if download_path.exists() or self.minio_client.download_dataset_file(object_name, download_path):
                return download_path
            raise FileNotFoundError(f"Failed to download image from MinIO: {object_name}")

        absolute = to_absolute_path(image_ref)
        if absolute and absolute.exists():
            return absolute

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

    def _format_decimal(self, value: float) -> str:
        return f"{value:.6f}".rstrip("0").rstrip(".") if "." in f"{value:.6f}" else f"{value:.6f}"
