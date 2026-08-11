package com.encore.notification_service.dto;

import java.util.UUID;

public record OrderStatusEvent(
        UUID orderId,
        boolean success,
        String failureReason
) {}