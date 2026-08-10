package com.encore.order_service.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID userId;

    @ElementCollection
    @CollectionTable(name = "order_seats", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "seat_id")
    private List<String> seatIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    @Column
    private OffsetDateTime updatedAt;
}