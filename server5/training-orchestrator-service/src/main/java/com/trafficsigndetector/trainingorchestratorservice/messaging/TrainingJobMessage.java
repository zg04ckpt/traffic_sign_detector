package com.trafficsigndetector.trainingorchestratorservice.messaging;

import com.trafficsigndetector.trainingorchestratorservice.model.MauHL;

import java.time.Instant;
import java.util.List;

public record TrainingJobMessage(
        String trackingId,
        String modelCode,
        Instant requestedAt,
        int epochs,
        int batchSize,
        double learningRate,
        int imageSize,
        int earlyStoppingPatience,
        String device,
        String optimizer,
        String modelPath,
        List<MauHL> samples
) {
    public TrainingJobMessage(String trackingId, String modelCode, Instant requestedAt) {
        this(
                trackingId,
                modelCode,
                requestedAt,
                20,
                32,
                0.01,
                416,
                5,
                "cpu",
                "Adam",
                "/models/yolov8s.pt",
                List.of()
        );
    }
}

