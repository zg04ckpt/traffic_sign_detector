package com.trafficsigndetector.trainingorchestratorservice.service.state;

import java.util.Locale;

public enum TrainingLifecycleState {
    CREATED,
    QUEUED,
    PREPARING_DATASET,
    STARTED,
    RUNNING,
    COMPLETED,
    FAILED,
    UNKNOWN;

    public String code() {
        return name();
    }

    public static TrainingLifecycleState fromExternal(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            return UNKNOWN;
        }

        try {
            return TrainingLifecycleState.valueOf(rawState.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
