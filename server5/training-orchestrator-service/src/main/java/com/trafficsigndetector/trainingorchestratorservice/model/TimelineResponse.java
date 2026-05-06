package com.trafficsigndetector.trainingorchestratorservice.model;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;

import java.util.List;

public record TimelineResponse(String trackingId, List<TrainingStatusMessage> events) {
}
