package com.nammametro.service;

import com.nammametro.event.BaseMetroEvent;
import com.nammametro.event.GateEntryExitEvent;
import com.nammametro.event.PaymentCompletedEvent;
import com.nammametro.event.TicketCreatedEvent;
import com.nammametro.event.TicketIssuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final InMemoryEventBus inMemoryEventBus;

    @Value("${nammametro.kafka.topics.ticket-events:ticket-booking-events}")
    private String ticketEventsTopic;

    @Value("${nammametro.kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    @Value("${nammametro.kafka.topics.gate-events:station-gate-events}")
    private String gateEventsTopic;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate, InMemoryEventBus inMemoryEventBus) {
        this.kafkaTemplate = kafkaTemplate;
        this.inMemoryEventBus = inMemoryEventBus;
    }

    public void sendTicketCreatedEvent(TicketCreatedEvent event) {
        log.info("[KAFKA PRODUCER] Publishing TicketCreatedEvent: ticket={}", event.getTicketNumber());
        sendToKafka(ticketEventsTopic, event.getTicketNumber(), event);
        inMemoryEventBus.publish(event);
    }

    public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("[KAFKA PRODUCER] Publishing PaymentCompletedEvent: tx={}, ticket={}", event.getTransactionId(), event.getTicketNumber());
        sendToKafka(paymentEventsTopic, event.getTransactionId(), event);
        inMemoryEventBus.publish(event);
    }

    public void sendTicketIssuedEvent(TicketIssuedEvent event) {
        log.info("[KAFKA PRODUCER] Publishing TicketIssuedEvent: ticket={}", event.getTicketNumber());
        sendToKafka(ticketEventsTopic, event.getTicketNumber(), event);
        inMemoryEventBus.publish(event);
    }

    public void sendGateEvent(GateEntryExitEvent event) {
        log.info("[KAFKA PRODUCER] Publishing GateEntryExitEvent: ticket={}, gate={}, allowed={}",
                event.getTicketNumber(), event.getGateType(), event.getIsAllowed());
        sendToKafka(gateEventsTopic, event.getTicketNumber(), event);
        inMemoryEventBus.publish(event);
    }

    private void sendToKafka(String topic, String key, BaseMetroEvent payload) {
        inMemoryEventBus.publish(payload);
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                if (kafkaTemplate != null) {
                    kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.debug("Kafka broker dispatch fallback for topic {}: {}", topic, ex.getMessage());
                        } else {
                            log.debug("Kafka ACK received: topic={}, partition={}, offset={}",
                                    topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
                }
            } catch (Exception e) {
                log.debug("Kafka broker dispatch exception: {}", e.getMessage());
            }
        });
    }
}
