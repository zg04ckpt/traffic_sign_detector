package com.trafficsigndetector.trainingorchestratorservice.service.strategy;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobMessage;
import com.trafficsigndetector.trainingorchestratorservice.model.MauHL;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultTrainingJobBuildStrategy implements TrainingJobBuildStrategy {

    @Override
    public boolean supports(ThongTinHLEntity session) {
        return true;
    }

    @Override
    public TrainingJobMessage build(
            ThongTinHLEntity session,
            String modelCode,
            String modelPath,
            List<MauHL> samples
    ) {
        return new TrainingJobMessage(
                session.getTrackingId(),
                modelCode,
                Instant.now(),
                session.getEpochs(),
                session.getBatchSize(),
                session.getLearningRate(),
                session.getKichThuocAnh(),
                session.getEarlyStoppingPatience(),
                session.getLoaiThietBi(),
                session.getOptimizer(),
                modelPath,
                new ArrayList<>(samples)
        );
    }
}

