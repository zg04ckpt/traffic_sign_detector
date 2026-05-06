package com.trafficsigndetector.trainingorchestratorservice.messaging;

public record TrainingSessionEventMessage(
        Integer id,
        String trackingId,
        String eventType,
        String trangThai,
        Integer currentEpoch,
        Double doChinhXac,
        Double doNhay,
        String publishedAt
) {
}
