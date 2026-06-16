package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class FailedTrainingSessionState implements TrainingSessionState {

    @Override
    public TrainingLifecycleState state() {
        return TrainingLifecycleState.FAILED;
    }

    @Override
    public void apply(ThongTinHLEntity session, TrainingStatusMessage message) {
        session.setTrangThai(TrainingLifecycleState.FAILED.code());
        session.setKetThucLuc(Instant.now());
    }
}
