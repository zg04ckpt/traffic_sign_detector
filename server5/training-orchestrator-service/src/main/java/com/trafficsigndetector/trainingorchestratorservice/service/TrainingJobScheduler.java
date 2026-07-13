package com.trafficsigndetector.trainingorchestratorservice.service;

import com.trafficsigndetector.sharedmodel.MauHL;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobPublisher;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import com.trafficsigndetector.trainingorchestratorservice.persistence.jdbc.TrainingSessionJdbcRepository;
import com.trafficsigndetector.trainingorchestratorservice.service.strategy.TrainingJobBuildStrategy;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingJobScheduler {

    private final TrainingJobPublisher trainingJobPublisher;
    private final TrainingSessionJdbcRepository trainingSessionJdbcRepository;
    private final List<TrainingJobBuildStrategy> trainingJobBuildStrategies;
    private final int maxConcurrentTraining;
    private final int maxQueuedTraining;

    public TrainingJobScheduler(
            TrainingJobPublisher trainingJobPublisher,
            TrainingSessionJdbcRepository trainingSessionJdbcRepository,
            List<TrainingJobBuildStrategy> trainingJobBuildStrategies,
            @Value("${app.training.admission.max-concurrent:1}") int maxConcurrentTraining,
            @Value("${app.training.admission.max-queued:10}") int maxQueuedTraining
    ) {
        this.trainingJobPublisher = trainingJobPublisher;
        this.trainingSessionJdbcRepository = trainingSessionJdbcRepository;
        this.trainingJobBuildStrategies = trainingJobBuildStrategies;
        this.maxConcurrentTraining = maxConcurrentTraining;
        this.maxQueuedTraining = maxQueuedTraining;
    }

    public void enforceAdmissionCapacity() {
        long running = trainingSessionJdbcRepository.countByTrangThaiIn(List.of("RUNNING", "PREPARING_DATASET"));
        long queued = trainingSessionJdbcRepository.countByTrangThai("QUEUED");

        if (running >= maxConcurrentTraining) {
            throw new QueueCapacityExceededException("Training workers are saturated. Please try again later.");
        }

        if (queued >= maxQueuedTraining) {
            throw new QueueCapacityExceededException("Training queue is full. Please try again later.");
        }
    }

    public void scheduleJob(ThongTinHLEntity session, String modelCode, String modelPath, List<MauHL> dsMau) {
        TrainingJobMessage job = resolveTrainingJobBuildStrategy(session)
                .build(session, modelCode, modelPath, dsMau);
        try {
            trainingJobPublisher.publish(job);
        } catch (AmqpException ex) {
            throw new QueueCapacityExceededException("Training queue is full. Please try again later.");
        }
    }

    private TrainingJobBuildStrategy resolveTrainingJobBuildStrategy(ThongTinHLEntity session) {
        for (TrainingJobBuildStrategy strategy : trainingJobBuildStrategies) {
            if (strategy.supports(session)) {
                return strategy;
            }
        }
        throw new IllegalStateException("No TrainingJobBuildStrategy supports this training session");
    }
}
