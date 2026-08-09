package com.encore.order_service.kafka.consumer;

import com.encore.order_service.dto.SeatHeldEvent;
import com.encore.order_service.kafka.Topics;
import com.encore.order_service.kafka.producer.OrderEventProducer;
import com.encore.order_service.order.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldConsumer {

    private final OrderService orderService;
    private final OrderEventProducer producer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {Topics.SEAT_HELD, Topics.SEAT_HOLD_FAILED}, groupId = "order-service")
    public void consume(String message) {
        try {
            SeatHeldEvent event = objectMapper.readValue(message, SeatHeldEvent.class);
            log.info("Received seat hold result for orderId {}: success={}", event.orderId(), event.success());

            if (event.success()) {
                orderService.confirmOrder(event.orderId());
                producer.sendOrderConfirmed(event, event.orderId().toString());
            } else {
                orderService.cancelOrder(event.orderId());
                producer.sendOrderCancelled(event, event.orderId().toString());
            }

        } catch (Exception e) {
            log.error("Failed to process seat hold event: {}", e.getMessage(), e);
        }
    }
}