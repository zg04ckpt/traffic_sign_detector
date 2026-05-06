package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class StartedTrainingSessionState implements TrainingSessionState {

    @Override
    public TrainingLifecycleState state() {
        return TrainingLifecycleState.STARTED;
    }

    @Override
    public void apply(ThongTinHLEntity session, TrainingStatusMessage message) {
        session.setTrangThai(TrainingLifecycleState.RUNNING.code());
        if (session.getBatDauLuc() == null) {
            session.setBatDauLuc(Instant.now());
        }
    }
}
