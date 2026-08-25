package com.nammametro.consumer;

import com.nammametro.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @KafkaListener(topics = "${nammametro.kafka.topics.payment-events:payment-events}",
                   groupId = "namma-metro-group",
                   autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[KAFKA CONSUMER: payment-events] Processing payment event: txId={}, ticket={}, amount=₹{}",
                event.getTransactionId(), event.getTicketNumber(), event.getAmount());
        // Simulates async passenger SMS / Email / WhatsApp confirmation trigger
    }
}
