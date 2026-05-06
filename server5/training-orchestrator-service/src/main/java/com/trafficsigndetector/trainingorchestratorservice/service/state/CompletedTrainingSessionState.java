package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CompletedTrainingSessionState implements TrainingSessionState {

    @Override
    public TrainingLifecycleState state() {
        return TrainingLifecycleState.COMPLETED;
    }

    @Override
    public void apply(ThongTinHLEntity session, TrainingStatusMessage message) {
        session.setTrangThai(TrainingLifecycleState.COMPLETED.code());
        if (session.getBatDauLuc() == null) {
            session.setBatDauLuc(Instant.now());
        }
        session.setKetThucLuc(Instant.now());
        if (session.getDuongDanMoHinhKetQua() == null || session.getDuongDanMoHinhKetQua().isBlank()) {
            session.setDuongDanMoHinhKetQua("/runtime/training/outputs/training-" + session.getId() + ".pt");
        }
    }
}
