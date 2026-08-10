package com.encore.inventory_service.kafka.consumer;

import com.encore.inventory_service.dto.SeatHoldRequestedEvent;
import com.encore.inventory_service.inventory.InventoryService;
import com.encore.inventory_service.kafka.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldRequestedConsumer {


    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    @KafkaListener(topics = Topics.SEAT_HOLD_REQUESTED, groupId = "inventory-service")
    public void consume(String message) {
        try {
            SeatHoldRequestedEvent event = objectMapper.readValue(message, SeatHoldRequestedEvent.class);
            log.info("Received seat hold request for orderId {}, for seats: {}", event.orderId(), event.seatIds());

            inventoryService.processHoldRequest(event);

        } catch (Exception e) {
            log.error("Failed to process seat hold request event: {}", e.getMessage(), e);
        }
    }
}
