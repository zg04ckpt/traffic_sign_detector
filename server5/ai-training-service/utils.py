import re
from pathlib import Path
from typing import Any, Dict, List, Optional

def as_int(value: Any, field_name: str) -> int:
    if value is None:
        raise ValueError(f"Missing required integer parameter: {field_name}")
    try:
        return int(value)
    except (TypeError, ValueError):
        raise ValueError(f"Invalid integer for {field_name}: {value}")

def as_float(value: Any, field_name: str) -> float:
    if value is None:
        raise ValueError(f"Missing required float parameter: {field_name}")
    try:
        return float(value)
    except (TypeError, ValueError):
        raise ValueError(f"Invalid float for {field_name}: {value}")

def as_string(value: Any, field_name: str) -> str:
    if value is None:
        raise ValueError(f"Missing required string parameter: {field_name}")
    text = str(value).strip()
    if not text:
        raise ValueError(f"String parameter {field_name} cannot be empty")
    return text

def case_insensitive_get(source: Dict[str, Any], key: str) -> Any:
    if key in source:
        return source[key]

    lowered = key.lower()
    for existing_key, value in source.items():
        if isinstance(existing_key, str) and existing_key.lower() == lowered:
            return value
    return None

def as_map(value: Any) -> Dict[str, Any]:
    if isinstance(value, dict):
        return value
    return {}

def normalize_list_of_maps(value: Any) -> List[Dict[str, Any]]:
    if not isinstance(value, list):
        return []

    result: List[Dict[str, Any]] = []
    for item in value:
        if isinstance(item, dict):
            result.append(item)
    return result

def to_absolute_path(raw_path: str) -> Optional[Path]:
    try:
        path = Path(raw_path)
    except Exception:
        return None

    if path.is_absolute():
        return path.resolve()
    return None

def extract_file_name(path_value: str) -> str:
    normalized = path_value.replace("\\", "/")
    without_query = normalized.split("?", 1)[0].split("#", 1)[0]
    return without_query.rsplit("/", 1)[-1]

def safe_file_name(value: str) -> str:
    safe = re.sub(r"[^A-Za-z0-9._-]", "_", value)
    return safe or "unknown"

def reset_dir(path: Path) -> None:
    if path.exists():
        for child in sorted(path.rglob("*"), reverse=True):
            if child.is_file() or child.is_symlink():
                child.unlink(missing_ok=True)
            elif child.is_dir():
                child.rmdir()
    path.mkdir(parents=True, exist_ok=True)
