package com.nammametro.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "spring.kafka.admin.auto-create", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    @Value("${nammametro.kafka.topics.ticket-events:ticket-booking-events}")
    private String ticketEventsTopic;

    @Value("${nammametro.kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    @Value("${nammametro.kafka.topics.gate-events:station-gate-events}")
    private String gateEventsTopic;

    @Bean
    public NewTopic ticketEventsTopic() {
        return TopicBuilder.name(ticketEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic gateEventsTopic() {
        return TopicBuilder.name(gateEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
