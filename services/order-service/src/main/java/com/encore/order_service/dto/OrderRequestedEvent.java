package com.encore.order_service.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderRequestedEvent(
        UUID orderId,
        UUID eventId,
        UUID userId,
        List<String> seatIds,
        OffsetDateTime requestedAt
) {}