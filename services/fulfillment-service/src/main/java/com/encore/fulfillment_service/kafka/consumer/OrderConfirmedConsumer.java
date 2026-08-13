package com.encore.fulfillment_service.kafka.consumer;

import com.encore.fulfillment_service.dto.OrderConfirmedEvent;
import com.encore.fulfillment_service.fulfillment.FulfillmentService;
import com.encore.fulfillment_service.kafka.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedConsumer {

    private final ObjectMapper objectMapper;
    private final FulfillmentService fulfillmentService;

    @KafkaListener(topics = Topics.ORDER_CONFIRMED, groupId = "fulfillment-service")
    public void consume(String message) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(message, OrderConfirmedEvent.class);
            log.info("Received order confirmed for orderId {}", event.orderId());

            fulfillmentService.processOrderConfirmedEvent(event);

        } catch (Exception e) {
            log.error("Failed to process order confirmed event: {}", e.getMessage(), e);
        }
    }
}
