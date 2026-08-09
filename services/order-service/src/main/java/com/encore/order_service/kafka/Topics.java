package com.encore.order_service.kafka;

public final class Topics {

    private Topics() {}

    public static final String ORDER_REQUESTED = "ticket.order.requested";
    public static final String SEAT_HOLD_REQUESTED = "ticket.seat.hold.requested";
    public static final String SEAT_HELD = "ticket.seat.held";
    public static final String SEAT_HOLD_FAILED = "ticket.seat.hold.failed";
    public static final String ORDER_CONFIRMED = "ticket.order.confirmed";
    public static final String ORDER_CANCELLED = "ticket.order.cancelled";
}