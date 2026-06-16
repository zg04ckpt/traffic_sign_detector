package com.trafficsigndetector.trainingorchestratorservice.messaging;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TrainingStatusTracker {
    private final Map<String, List<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    public void onStatus(TrainingStatusMessage message) {
        List<SseEmitter> emitters = subscribers.getOrDefault(message.trackingId(), List.of());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("training-status")
                        .data(message));
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    public SseEmitter subscribe(String trackingId) {
        SseEmitter emitter = new SseEmitter(0L);
        subscribers.computeIfAbsent(trackingId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeSubscriber(trackingId, emitter));
        emitter.onTimeout(() -> removeSubscriber(trackingId, emitter));
        emitter.onError(ex -> removeSubscriber(trackingId, emitter));

        return emitter;
    }

    private void removeSubscriber(String trackingId, SseEmitter emitter) {
        List<SseEmitter> emitters = subscribers.get(trackingId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            subscribers.remove(trackingId);
        }
    }
}
