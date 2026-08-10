package com.encore.inventory_service.dto;

import java.util.List;
import java.util.UUID;

public record SeatHeldEvent(
    UUID orderId,
    boolean success,
    String failureReason
) {}
