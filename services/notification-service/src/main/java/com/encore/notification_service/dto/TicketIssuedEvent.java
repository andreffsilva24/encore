package com.encore.notification_service.dto;

import java.util.List;
import java.util.UUID;

public record TicketIssuedEvent(
        UUID orderId,
        UUID userId,
        List<String> seatIds,
        String encodedTicketData
) {}
