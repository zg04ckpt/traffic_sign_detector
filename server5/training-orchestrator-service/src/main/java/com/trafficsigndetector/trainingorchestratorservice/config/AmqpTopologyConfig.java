package com.trafficsigndetector.trainingorchestratorservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpTopologyConfig {

    @Bean
    DirectExchange trainingExchange(@Value("${app.rabbitmq.exchange.training}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue trainingRequestQueue(
            @Value("${app.rabbitmq.queue.training-request}") String queueName,
            @Value("${app.rabbitmq.queue-max-length.training-request:10}") int maxLength
    ) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-max-length", maxLength)
                .withArgument("x-overflow", "reject-publish")
                .build();
    }

    @Bean
    Queue trainingStatusQueue(@Value("${app.rabbitmq.queue.training-status}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding trainingRequestBinding(
            DirectExchange trainingExchange,
            Queue trainingRequestQueue,
            @Value("${app.rabbitmq.routing-key.training-request}") String routingKey
    ) {
        return BindingBuilder.bind(trainingRequestQueue).to(trainingExchange).with(routingKey);
    }

    @Bean
    Binding trainingStatusBinding(
            DirectExchange trainingExchange,
            Queue trainingStatusQueue,
            @Value("${app.rabbitmq.routing-key.training-status}") String routingKey
    ) {
        return BindingBuilder.bind(trainingStatusQueue).to(trainingExchange).with(routingKey);
    }

    @Bean
    MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
