package com.encore.notification_service.kafka.consumer;

import com.encore.notification_service.dto.OrderStatusEvent;
import com.encore.notification_service.dto.TicketIssuedEvent;
import com.encore.notification_service.kafka.Topics;
import com.encore.notification_service.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketIssuedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.TICKET_ISSUED, groupId = "notification-service")
    public void consume(String message) {
        try {
            TicketIssuedEvent ticketIssuedEvent = objectMapper.readValue(message, TicketIssuedEvent.class);
            log.info("[NOTIFICATION] Received ticket issued event for orderId {}", ticketIssuedEvent.orderId());
            notificationService.sendTicketIssued(ticketIssuedEvent);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to process order failed event: {}", e.getMessage(), e);
        }
    }
}
