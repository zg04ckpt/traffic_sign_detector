package com.trafficsigndetector.trainingorchestratorservice.service.strategy;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobMessage;
import com.trafficsigndetector.trainingorchestratorservice.model.MauHL;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;

import java.util.List;

public interface TrainingJobBuildStrategy {

    boolean supports(ThongTinHLEntity session);

    TrainingJobMessage build(
            ThongTinHLEntity session,
            String modelCode,
            String modelPath,
            List<MauHL> samples
    );
}

