package com.encore.order_service.order;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID eventId,
        UUID userId,
        List<String> seatIds,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getEventId(),
                order.getUserId(),
                List.copyOf(order.getSeatIds()),
                order.getStatus().name(),
                order.getRequestedAt(),
                order.getUpdatedAt()
        );
    }
}