package com.nammametro.consumer;

import com.nammametro.event.GateEntryExitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GateEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(GateEventConsumer.class);

    @KafkaListener(topics = "${nammametro.kafka.topics.gate-events:station-gate-events}",
                   groupId = "namma-metro-group",
                   autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleGateEvents(GateEntryExitEvent event) {
        log.info("[KAFKA CONSUMER: station-gate-events] Gate audit: station={}, gate={}, ticket={}, allowed={}, penalty=₹{}",
                event.getStationName(), event.getGateType(), event.getTicketNumber(), event.getIsAllowed(), event.getPenaltyCharged());
    }
}
