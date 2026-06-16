package com.trafficsigndetector.trainingorchestratorservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsigndetector.trainingorchestratorservice.persistence.entity.ThongTinHLEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Publish training events untuk sync ke SQLite cache ở services.
 * Subscribe: aimodel-service, dataset-service, ai-training-service
 */
@Service
@Slf4j
public class TrainingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange.training:training.exchange}")
    private String trainingExchange;

    @Value("${app.rabbitmq.routing-key.training-status:training.status}")
    private String statusRoutingKey;

    public TrainingEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish training session event
     */
    public void publishSessionEvent(ThongTinHLEntity session, String eventType) {
        try {
            TrainingSessionEventMessage payload = new TrainingSessionEventMessage(
                session.getId(),
                session.getTrackingId(),
                eventType,
                session.getTrangThai(),
                session.getCurrentEpoch(),
                session.getDoChinhXac(),
                session.getDoNhay(),
                Instant.now().toString()
            );

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(trainingExchange, statusRoutingKey, message);

            log.debug("Published training event: id={}, type={}", session.getId(), eventType);
        } catch (Exception ex) {
            log.error("Failed to publish training event: id={}, type={}, error={}", 
                    session.getId(), eventType, ex.getMessage(), ex);
        }
    }
}
