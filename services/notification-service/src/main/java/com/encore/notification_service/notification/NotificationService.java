package com.encore.notification_service.notification;

import com.encore.notification_service.dto.OrderStatusEvent;
import com.encore.notification_service.dto.TicketIssuedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${services.user-service-url}")
    private String userServiceURL;

    @Value("${services.order-service-url}")
    private String orderServiceURL;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.url}")
    private String resendUrl;


    public void sendTicketIssued(@NonNull TicketIssuedEvent ticketIssuedEvent) {

        String email = getUserEmail(ticketIssuedEvent.userId());
        if (email == null) return;

        String body = String.format(
                "<h2>Your ticket is ready!</h2>" +
                        "<p>Order ID: <strong>%s</strong></p>" +
                        "<p>Seats: <strong>%s</strong></p>" +
                        "<p>Present this QR code at the venue:</p>" +
                        "<img src='data:image/png;base64,%s' alt='Your ticket QR code' width='300' height='300'/>",
                ticketIssuedEvent.orderId(),
                String.join(", ", ticketIssuedEvent.seatIds()),
                ticketIssuedEvent.encodedTicketData()
        );

        sendEmail(email, "Ticket Issued — Encore", body);
        log.info("[NOTIFICATION] Ticket issued email sent for order {}", ticketIssuedEvent.orderId());
    }

    public void sendOrderConfirmed(@NonNull OrderStatusEvent event) {
        UUID userId = getUserIdFromOrder(event.orderId());
        if (userId == null) return;

        String email = getUserEmail(userId);
        if (email == null) return;

        String body = String.format(
                "<h2>Your order is confirmed!</h2>" +
                        "<p>Order ID: <strong>%s</strong></p>" +
                        "<p>Your tickets are being generated.</p>",
                event.orderId()
        );

        sendEmail(email, "Order Confirmed — Encore", body);
        log.info("[NOTIFICATION] Order confirmed email sent for order {}", event.orderId());
    }

    public void sendOrderFailed(@NonNull OrderStatusEvent event) {
        UUID userId = getUserIdFromOrder(event.orderId());
        if (userId == null) return;

        String email = getUserEmail(userId);
        if (email == null) return;

        String body = String.format(
                "<h2>Your order has failed!</h2>" +
                        "<p>Order ID: <strong>%s</strong></p>" +
                        "<p>Reason: <strong>%s</strong></p>",
                event.orderId(), event.failureReason()
        );

        sendEmail(email, "Order Failed — Encore", body);
        log.info("[NOTIFICATION] Order failed email sent for order {}", event.orderId());
    }

    public void sendOrderCancelled(@NonNull OrderStatusEvent event) {
        UUID userId = getUserIdFromOrder(event.orderId());
        if (userId == null) return;

        String email = getUserEmail(userId);
        if (email == null) return;

        String body = String.format(
                "<h2>Your order has been cancelled!</h2>" +
                        "<p>Order ID: <strong>%s</strong></p>" +
                        "<p>Reason: <strong>%s</strong></p>",
                event.orderId(), event.failureReason()
        );

        sendEmail(email, "Order Cancelled — Encore", body);
        log.info("[NOTIFICATION] Order cancelled email sent for order {}", event.orderId());
    }

    private @Nullable UUID getUserIdFromOrder(UUID orderId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(orderServiceURL + "/api/orders/" + orderId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return UUID.fromString(json.get("userId").asText());
            }

            log.warn("[NOTIFICATION] Order {} not found in Order Service, status: {}", orderId, response.statusCode());
            return null;
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to fetch userId for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    private @Nullable String getUserEmail(UUID userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(userServiceURL + "/api/users/" + userId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return json.get("email").asText();
            }

            log.warn("[NOTIFICATION] User {} not found in User Service, status: {}", userId, response.statusCode());
            return null;
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to fetch email for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from", "Encore <onboarding@resend.dev>",
                    "to", List.of(to),
                    "subject", subject,
                    "html", body
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resendUrl))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("[NOTIFICATION] Email sent to {} with subject '{}'", to, subject);
            } else {
                log.error("[NOTIFICATION] Failed to send email to {}: status {} body {}", to, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
