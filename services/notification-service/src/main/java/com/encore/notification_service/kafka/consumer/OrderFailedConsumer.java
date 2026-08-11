package com.encore.notification_service.kafka.consumer;

import com.encore.notification_service.dto.OrderStatusEvent;
import com.encore.notification_service.kafka.Topics;
import com.encore.notification_service.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFailedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.SEAT_HOLD_FAILED, groupId = "notification-service")
    public void consume(String message) {
        try {
            OrderStatusEvent orderStatusEvent = objectMapper.readValue(message, OrderStatusEvent.class);
            log.info("[NOTIFICATION] Received order failed event for orderId {}", orderStatusEvent.orderId());
            notificationService.sendOrderFailed(orderStatusEvent);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to process order failed event: {}", e.getMessage(), e);
        }
    }
}
