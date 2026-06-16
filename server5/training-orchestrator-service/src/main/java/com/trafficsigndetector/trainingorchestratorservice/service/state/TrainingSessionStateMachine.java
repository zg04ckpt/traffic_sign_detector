package com.trafficsigndetector.trainingorchestratorservice.service.state;

import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TrainingSessionStateMachine {

    private final Map<TrainingLifecycleState, TrainingSessionState> states;
    private final TrainingSessionState fallbackState;

    public TrainingSessionStateMachine(List<TrainingSessionState> stateHandlers) {
        this.states = new EnumMap<>(TrainingLifecycleState.class);
        for (TrainingSessionState stateHandler : stateHandlers) {
            states.put(stateHandler.state(), stateHandler);
        }
        this.fallbackState = states.getOrDefault(TrainingLifecycleState.UNKNOWN, new GenericTrainingSessionState());
    }

    public void apply(ThongTinHLEntity session, TrainingStatusMessage message) {
        TrainingLifecycleState lifecycleState = TrainingLifecycleState.fromExternal(message.state());
        TrainingSessionState stateHandler = states.getOrDefault(lifecycleState, fallbackState);
        stateHandler.apply(session, message);
    }
}
