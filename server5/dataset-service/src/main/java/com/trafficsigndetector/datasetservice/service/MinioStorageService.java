package com.trafficsigndetector.datasetservice.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(MinioClient minioClient, @Value("${minio.bucket.datasets}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        initBucket();
    }

    private void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket: " + bucketName, e);
        }
    }

    public String storeFile(MultipartFile file, String datasetName) {
        try (InputStream inputStream = file.getInputStream()) {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".") ? 
                    originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            
            // Format: datasets/{datasetName}/{uuid}{extension}
            String objectName = String.format("%s/%s%s", 
                    datasetName.replaceAll("[^a-zA-Z0-9-]", "_"), 
                    UUID.randomUUID(), 
                    extension);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return String.format("/%s/%s", bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            // Remove leading slash and bucket name if present
            if (objectName.startsWith("/" + bucketName + "/")) {
                objectName = objectName.substring(bucketName.length() + 2);
            }
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            // Best effort
        }
    }
}
