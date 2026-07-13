from typing import Optional
from pathlib import Path
from minio import Minio
from minio.error import S3Error
import logging

from settings import WorkerSettings

LOGGER = logging.getLogger("minio-client")

class MinioClientWrapper:
    def __init__(self, settings: WorkerSettings):
        self.settings = settings
        self.client = Minio(
            self.settings.minio_endpoint,
            access_key=self.settings.minio_access_key,
            secret_key=self.settings.minio_secret_key,
            secure=self.settings.minio_secure,
        )
        self._ensure_buckets()

    def _ensure_buckets(self):
        for bucket in [self.settings.minio_datasets_bucket, self.settings.minio_models_bucket]:
            try:
                if not self.client.bucket_exists(bucket):
                    self.client.make_bucket(bucket)
                    LOGGER.info("Created MinIO bucket: %s", bucket)
            except S3Error as err:
                LOGGER.error("Error creating bucket %s: %s", bucket, err)

    def download_dataset_file(self, object_name: str, file_path: Path) -> bool:
        try:
            self.client.fget_object(self.settings.minio_datasets_bucket, object_name, str(file_path))
            return True
        except S3Error as err:
            LOGGER.error("Error downloading file %s from bucket %s: %s", object_name, self.settings.minio_datasets_bucket, err)
            return False

    def upload_model(self, file_path: Path, object_name: str) -> Optional[str]:
        try:
            self.client.fput_object(
                self.settings.minio_models_bucket,
                object_name,
                str(file_path)
            )
            return f"/{self.settings.minio_models_bucket}/{object_name}"
        except S3Error as err:
            LOGGER.error("Error uploading model %s to bucket %s: %s", object_name, self.settings.minio_models_bucket, err)
            return None
