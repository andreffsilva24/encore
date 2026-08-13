package com.encore.fulfillment_service.dto;

import java.util.UUID;

public record OrderConfirmedEvent(
        UUID orderId,
        boolean success,
        String failureReason
) {}
