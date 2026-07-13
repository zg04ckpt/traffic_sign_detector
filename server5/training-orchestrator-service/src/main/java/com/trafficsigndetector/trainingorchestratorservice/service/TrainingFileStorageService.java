package com.trafficsigndetector.trainingorchestratorservice.service;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TrainingFileStorageService {

    private final MinioClient minioClient;
    private final String aimodelModelPublicPrefix;
    private final String bucketName;

    public TrainingFileStorageService(
            MinioClient minioClient,
            @Value("${app.training.artifact.aimodel-public-prefix}") String aimodelModelPublicPrefix,
            @Value("${minio.bucket.models}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.aimodelModelPublicPrefix = aimodelModelPublicPrefix;
        this.bucketName = bucketName;
    }

    public String buildTargetFileName(String versionName, String sourceFileName) {
        String extension = ".pt";
        int dotIndex = sourceFileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < sourceFileName.length() - 1) {
            extension = sourceFileName.substring(dotIndex);
        }

        String safeBase = versionName == null ? "version" : versionName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeBase.isBlank()) {
            safeBase = "version";
        }
        if (safeBase.length() > 80) {
            safeBase = safeBase.substring(0, 80);
        }

        return safeBase + extension;
    }

    public String normalizeAimodelPublicPath(int moHinhId, String fileName) {
        String prefix = aimodelModelPublicPrefix.endsWith("/")
                ? aimodelModelPublicPrefix.substring(0, aimodelModelPublicPrefix.length() - 1)
                : aimodelModelPublicPrefix;
        return prefix + "/mo-hinh-" + moHinhId + "/" + fileName;
    }

    public String copyToAimodelStore(String sourceMinioPath, int moHinhId, String targetFileName) {
        try {
            // Remove prefix if present e.g. /models/
            String sourceObjectName = sourceMinioPath;
            if (sourceObjectName.startsWith("/" + bucketName + "/")) {
                sourceObjectName = sourceObjectName.substring(bucketName.length() + 2);
            }
            
            String targetObjectName = "mo-hinh-" + moHinhId + "/" + targetFileName;

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetObjectName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(sourceObjectName)
                                            .build()
                            )
                            .build()
            );
            return targetObjectName;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not copy model artifact in MinIO", ex);
        }
    }

    public void deleteTempArtifact(String tempModelPath) {
        try {
            String sourceObjectName = tempModelPath;
            if (sourceObjectName.startsWith("/" + bucketName + "/")) {
                sourceObjectName = sourceObjectName.substring(bucketName.length() + 2);
            }
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(sourceObjectName).build());
        } catch (Exception ex) {
            throw new IllegalStateException("Saved version but could not delete temporary model artifact: " + tempModelPath, ex);
        }
    }

    public void deleteIfExistsQuietly(String minioPath) {
        try {
            String objectName = minioPath;
            if (objectName.startsWith("/" + bucketName + "/")) {
                objectName = objectName.substring(bucketName.length() + 2);
            }
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception ignored) {
            // Best-effort cleanup only.
        }
    }
    
    public String extractFileName(String input) {
        int slash = input.lastIndexOf('/');
        return slash >= 0 ? input.substring(slash + 1) : input;
    }
}
