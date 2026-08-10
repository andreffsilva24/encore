package com.encore.inventory_service.inventory;

import com.encore.inventory_service.dto.SeatHeldEvent;
import com.encore.inventory_service.dto.SeatHoldRequestedEvent;
import com.encore.inventory_service.kafka.producer.InventoryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryEventProducer inventoryEventProducer;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String SEAT_HOLD_KEY = "seat-hold:%s:%s";

    @Value("${inventory.seat-hold-ttl-minutes}")
    private int seatHoldTTLMinutes;

    public void processHoldRequest(SeatHoldRequestedEvent event) {
        Expiration expiration = Expiration.from(seatHoldTTLMinutes, TimeUnit.MINUTES);
        List<String> reservedSeats = new ArrayList<>();
        for (String seatID: event.seatIds()){
            String key = String.format(SEAT_HOLD_KEY, event.eventId(), seatID);
            Boolean seatReserved = stringRedisTemplate
                    .opsForValue()
                    .setIfAbsent(key, event.orderId().toString(), expiration);

            if (!Boolean.TRUE.equals(seatReserved)) {
                reservedSeats.forEach(seatId -> {
                    String reservedKey = String.format(SEAT_HOLD_KEY, event.eventId(), seatId);
                    stringRedisTemplate.delete(reservedKey);
                });

                SeatHeldEvent seatHeldEvent = new SeatHeldEvent(event.orderId(), false, "Seat " + seatID + " is already taken");
                inventoryEventProducer.sendSeatHoldFailed(seatHeldEvent, event.orderId().toString());
                log.error("Failed to hold seats for order {}: {}", event.orderId(), event.seatIds());
                return;
            }

            reservedSeats.add(seatID);
        }

        SeatHeldEvent seatHeldEvent = new SeatHeldEvent(event.orderId(), true, null);
        inventoryEventProducer.sendSeatHeld(seatHeldEvent, event.orderId().toString());
        log.info("Successfully held seats for order {}: {}", event.orderId(), event.seatIds());
    }
}
