package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

@Component
public class GenericTrainingSessionState implements TrainingSessionState {

    @Override
    public TrainingLifecycleState state() {
        return TrainingLifecycleState.UNKNOWN;
    }

    @Override
    public void apply(ThongTinHLEntity session, TrainingStatusMessage message) {
        session.setTrangThai(TrainingLifecycleState.fromExternal(message.state()).code());
    }
}
