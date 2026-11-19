package com.encore.order_service.order;

import com.encore.order_service.dto.OrderRequestedEvent;
import com.encore.order_service.exceptions.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void createOrder(OrderRequestedEvent event) {
        Order order = new Order();
        order.setOrderId(event.orderId());
        order.setEventId(event.eventId());
        order.setUserId(event.userId());
        order.setSeatIds(event.seatIds());
        order.setStatus(OrderStatus.PENDING);
        order.setRequestedAt(event.requestedAt());

        orderRepository.save(order);
        log.info("Order {} created with status PENDING", order.getOrderId());
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        updateStatus(orderId, OrderStatus.CONFIRMED);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        updateStatus(orderId, OrderStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrderResponseById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findOrderResponsesByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    private void updateStatus(UUID orderId, OrderStatus status) {
        orderRepository
                .findById(orderId)
                .ifPresentOrElse(order ->
                        {
                            order.setStatus(status);
                            order.setUpdatedAt(OffsetDateTime.now());
                            orderRepository.save(order);
                            log.info("Order {} transitioned to {}", orderId, status);
                        },
                        () -> log.warn("Order {} not found for status update to {}", orderId, status));
    }
}