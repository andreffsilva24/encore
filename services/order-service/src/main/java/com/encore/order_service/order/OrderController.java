package com.encore.order_service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.findOrderResponseById(orderId));
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestParam UUID userId) {
        return ResponseEntity.ok(orderService.findOrderResponsesByUserId(userId));
    }
}