package com.trafficsigndetector.trainingorchestratorservice.messaging;

import com.trafficsigndetector.trainingorchestratorservice.service.TrainingSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TrainingStatusListener {

    private static final Logger log = LoggerFactory.getLogger(TrainingStatusListener.class);

    private final TrainingStatusTracker trainingStatusTracker;
    private final TrainingSessionService trainingSessionService;

    public TrainingStatusListener(
            TrainingStatusTracker trainingStatusTracker,
            TrainingSessionService trainingSessionService
    ) {
        this.trainingStatusTracker = trainingStatusTracker;
        this.trainingSessionService = trainingSessionService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.training-status}")
    public void handleStatus(TrainingStatusMessage message) {
        log.info("Received training status. trackingId={}, state={}, detail={}",
                message.trackingId(), message.state(), message.detail());
        trainingStatusTracker.onStatus(message);
        trainingSessionService.onStatus(message);
    }
}
