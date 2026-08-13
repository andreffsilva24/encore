package com.encore.fulfillment_service.dto;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

public record TicketIssuedEvent(
        UUID orderId,
        UUID userId,
        List<String> seatIds,
        String encodedTicketData
) {}
