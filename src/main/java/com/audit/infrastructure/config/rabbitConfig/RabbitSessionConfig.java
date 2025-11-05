package com.audit.infrastructure.config.rabbitConfig;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitSessionConfig {

    public static final String AUDIT_EXCHANGE = "audit.exchange";
    public static final String SESSION_AUDIT_QUEUE = "audit.session.queue";
    public static final String SESSION_EVENT_ROUTING_KEY = "session.event";

    // spring bean for rabbitmq exchange
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE, true, false);
    }

    // spring bean for rabbitmq queue
    @Bean
    public Queue sessionAuditQueue() {
        return QueueBuilder.durable(SESSION_AUDIT_QUEUE).build();
    }

    @Bean
    public Binding sessionEventBinding(Queue sessionAuditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(sessionAuditQueue)
                .to(auditExchange)
                .with(SESSION_EVENT_ROUTING_KEY);
    }
}
