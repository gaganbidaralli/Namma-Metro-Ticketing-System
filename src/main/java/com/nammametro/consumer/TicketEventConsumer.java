package com.nammametro.consumer;

import com.nammametro.event.TicketCreatedEvent;
import com.nammametro.event.TicketIssuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TicketEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketEventConsumer.class);

    @KafkaListener(topics = "${nammametro.kafka.topics.ticket-events:ticket-booking-events}",
                   groupId = "namma-metro-group",
                   autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleTicketEvents(Object event) {
        if (event instanceof TicketCreatedEvent createdEvent) {
            log.info("[KAFKA CONSUMER: ticket-events] Order registered: ticket={}, user={}, amount=₹{}",
                    createdEvent.getTicketNumber(), createdEvent.getUserId(), createdEvent.getAmount());
        } else if (event instanceof TicketIssuedEvent issuedEvent) {
            log.info("[KAFKA CONSUMER: ticket-events] Ticket issued & valid until: ticket={}, validUntil={}",
                    issuedEvent.getTicketNumber(), issuedEvent.getValidUntil());
        }
    }
}
