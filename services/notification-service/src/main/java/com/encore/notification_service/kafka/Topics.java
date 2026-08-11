package com.encore.notification_service.kafka;

public final class Topics {

    private Topics() {}

    public static final String ORDER_CONFIRMED = "ticket.order.confirmed";
    public static final String TICKET_ISSUED = "ticket.issued";
    public static final String SEAT_HOLD_FAILED = "ticket.seat.hold.failed";
    public static final String ORDER_CANCELLED = "ticket.order.cancelled";
}