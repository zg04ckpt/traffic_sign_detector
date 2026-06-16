package com.trafficsigndetector.trainingorchestratorservice.service;

public class QueueCapacityExceededException extends RuntimeException {

    public QueueCapacityExceededException(String message) {
        super(message);
    }
}