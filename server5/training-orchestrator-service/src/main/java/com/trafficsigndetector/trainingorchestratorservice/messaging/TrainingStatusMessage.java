package com.trafficsigndetector.trainingorchestratorservice.messaging;

import java.time.Instant;

public record TrainingStatusMessage(
        String trackingId,
        String state,
        String detail,
        Instant updatedAt,
        Integer currentEpoch,
        Double precision,
        Double recall,
        String logLine,
        String modelArtifactPath
) {
        public TrainingStatusMessage(String trackingId, String state, String detail, Instant updatedAt) {
                this(trackingId, state, detail, updatedAt, null, null, null, null, null);
        }
}
