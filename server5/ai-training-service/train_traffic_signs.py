from __future__ import annotations

import logging
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Optional

from ultralytics import YOLO


ProgressCallback = Callable[[str, Optional[int], Optional[float], Optional[float]], None]
LOGGER = logging.getLogger("training-traffic-signs")


@dataclass
class TrainingConfig:
    model_path: str
    data_yaml_path: Path
    project_dir: Path
    run_name: str
    epochs: int
    batch_size: int
    learning_rate: float
    image_size: int
    patience: int
    device: str
    optimizer: str


@dataclass
class TrainingResult:
    current_epoch: Optional[int]
    precision: Optional[float]
    recall: Optional[float]
    best_model_path: Path


def train_model(config: TrainingConfig, on_progress: Optional[ProgressCallback] = None) -> TrainingResult:
    _configure_offline_font_fallback()
    model = YOLO(config.model_path)
    _register_epoch_callbacks(model, config.epochs, on_progress)

    train_result = model.train(
        data=str(config.data_yaml_path),
        epochs=config.epochs,
        imgsz=config.image_size,
        batch=config.batch_size,
        device=_normalize_device(config.device),
        patience=config.patience,
        optimizer=config.optimizer,
        lr0=config.learning_rate,
        project=str(config.project_dir),
        name=config.run_name,
        exist_ok=True,
        verbose=False,
        save=True,
        pretrained=True,
        workers=2,
        plots=False,
    )

    validation_metrics = model.val(
        data=str(config.data_yaml_path),
        imgsz=config.image_size,
        device=_normalize_device(config.device),
        verbose=False,
    )

    precision = _extract_metric(validation_metrics, "precision")
    recall = _extract_metric(validation_metrics, "recall")
    best_model_path = _locate_best_model_path(config, train_result)

    return TrainingResult(
        current_epoch=config.epochs,
        precision=precision,
        recall=recall,
        best_model_path=best_model_path,
    )


def _register_epoch_callbacks(model: YOLO, total_epochs: int, on_progress: Optional[ProgressCallback]) -> None:
    if on_progress is None:
        return

    def emit_epoch_progress(trainer) -> None:
        raw_epoch = getattr(trainer, "epoch", None)
        current_epoch = None
        if isinstance(raw_epoch, int):
            current_epoch = raw_epoch + 1
        details = f"Epoch {current_epoch}/{total_epochs}" if current_epoch else "Training in progress"

        metrics = getattr(trainer, "metrics", None)
        precision = _extract_metric(metrics, "precision")
        recall = _extract_metric(metrics, "recall")
        on_progress(details, current_epoch, precision, recall)

    for event_name in ("on_train_epoch_end", "on_fit_epoch_end"):
        try:
            model.add_callback(event_name, emit_epoch_progress)
        except Exception:
            continue


def _locate_best_model_path(config: TrainingConfig, train_result) -> Path:
    save_dir = getattr(train_result, "save_dir", None)
    if save_dir:
        save_path = Path(str(save_dir))
        direct = save_path / "weights" / "best.pt"
        if direct.exists():
            return direct

    default_candidate = config.project_dir / config.run_name / "weights" / "best.pt"
    if default_candidate.exists():
        return default_candidate

    best_candidates = sorted(
        config.project_dir.rglob("best.pt"),
        key=lambda path: path.stat().st_mtime,
        reverse=True,
    )
    if best_candidates:
        return best_candidates[0]

    raise FileNotFoundError("Could not locate best.pt in training output")


def _extract_metric(metrics_obj, metric_name: str) -> Optional[float]:
    if metrics_obj is None:
        return None

    if metric_name == "precision":
        value = _first_not_none(
            _nested_getattr(metrics_obj, "box.mp"),
            _read_results_dict(metrics_obj, "metrics/precision(B)"),
            _read_results_dict(metrics_obj, "metrics/precision"),
        )
    elif metric_name == "recall":
        value = _first_not_none(
            _nested_getattr(metrics_obj, "box.mr"),
            _read_results_dict(metrics_obj, "metrics/recall(B)"),
            _read_results_dict(metrics_obj, "metrics/recall"),
        )
    else:
        value = None

    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _read_results_dict(metrics_obj, key: str):
    result_dict = getattr(metrics_obj, "results_dict", None)
    if isinstance(result_dict, dict):
        return result_dict.get(key)
    return None


def _nested_getattr(obj, path: str):
    current = obj
    for part in path.split("."):
        current = getattr(current, part, None)
        if current is None:
            return None
    return current


def _first_not_none(*values):
    for value in values:
        if value is not None:
            return value
    return None


def _normalize_device(device: str) -> str:
    value = (device or "cpu").strip().lower()
    if value in {"gpu", "cuda"}:
        return "0"
    if value.startswith("cuda"):
        return value
    return "cpu"


def _configure_offline_font_fallback() -> None:
    """Prevent Ultralytics from failing when Arial.ttf cannot be downloaded offline."""
    try:
        from ultralytics.data import utils as data_utils
        from ultralytics.utils import checks
    except Exception:
        return

    current_check = getattr(checks, "check_font", None)
    if current_check is None or getattr(current_check, "_offline_safe", False):
        return

    def safe_check_font(font: str = "Arial.ttf"):
        try:
            return current_check(font)
        except Exception as exc:
            LOGGER.warning(
                "Skipping Ultralytics font download for %s due to offline environment: %s",
                font,
                exc,
            )
            return font

    safe_check_font._offline_safe = True  # type: ignore[attr-defined]
    checks.check_font = safe_check_font
    data_utils.check_font = safe_check_font