package com.encore.order_service.kafka.consumer;

import com.encore.order_service.dto.OrderRequestedEvent;
import com.encore.order_service.dto.SeatHoldRequestedEvent;
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
public class OrderRequestedConsumer {

    private final OrderService orderService;
    private final OrderEventProducer producer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.ORDER_REQUESTED, groupId = "order-service")
    public void consume(String message) {
        try {
            OrderRequestedEvent event = objectMapper.readValue(message, OrderRequestedEvent.class);
            log.info("Received order request for orderId {}", event.orderId());

            orderService.createOrder(event);

            SeatHoldRequestedEvent seatHoldEvent = new SeatHoldRequestedEvent(
                    event.orderId(),
                    event.eventId(),
                    event.seatIds()
            );

            producer.sendSeatHoldRequested(seatHoldEvent, event.orderId().toString());

        } catch (Exception e) {
            log.error("Failed to process order requested event: {}", e.getMessage(), e);
        }
    }
}