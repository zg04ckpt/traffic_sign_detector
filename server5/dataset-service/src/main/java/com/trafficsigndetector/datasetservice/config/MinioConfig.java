package com.trafficsigndetector.datasetservice.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url:http://minio:9000}")
    private String minioUrl;

    @Value("${minio.access.name:minioadmin}")
    private String accessKey;

    @Value("${minio.access.secret:minioadmin123}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}
