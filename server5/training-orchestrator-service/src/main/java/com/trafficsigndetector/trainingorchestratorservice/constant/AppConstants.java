package com.trafficsigndetector.trainingorchestratorservice.constant;

public final class AppConstants {
    
    private AppConstants() {
        // Prevent instantiation
    }

    public static final String STATE_CREATED = "CREATED";
    public static final String STATE_QUEUED = "QUEUED";
    public static final String STATE_RUNNING = "RUNNING";
    public static final String STATE_PREPARING_DATASET = "PREPARING_DATASET";
    public static final String STATE_COMPLETED = "COMPLETED";
    public static final String STATE_FAILED = "FAILED";
}
