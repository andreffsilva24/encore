package com.encore.order_service.dto;

import java.util.UUID;

public record SeatHeldEvent(
        UUID orderId,
        boolean success,
        String failureReason
) {}