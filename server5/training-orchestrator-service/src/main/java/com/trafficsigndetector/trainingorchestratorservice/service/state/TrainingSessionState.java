package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;

public interface TrainingSessionState {

    TrainingLifecycleState state();

    void apply(ThongTinHLEntity session, TrainingStatusMessage message);
}
