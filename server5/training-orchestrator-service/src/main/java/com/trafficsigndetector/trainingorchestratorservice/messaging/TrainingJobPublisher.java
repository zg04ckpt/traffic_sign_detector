package com.trafficsigndetector.trainingorchestratorservice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrainingJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public TrainingJobPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange.training}") String exchange,
            @Value("${app.rabbitmq.routing-key.training-request}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(TrainingJobMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
