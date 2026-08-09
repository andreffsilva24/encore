package com.encore.order_service.dto;

import java.util.List;
import java.util.UUID;

public record SeatHoldRequestedEvent(
        UUID orderId,
        UUID eventId,
        List<String> seatIds
) {}