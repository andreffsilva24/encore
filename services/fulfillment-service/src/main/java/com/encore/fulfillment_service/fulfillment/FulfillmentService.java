package com.encore.fulfillment_service.fulfillment;

import com.encore.fulfillment_service.dto.OrderConfirmedEvent;
import com.encore.fulfillment_service.dto.TicketIssuedEvent;
import com.encore.fulfillment_service.kafka.producer.FulfillmentEventProducer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentService {

    private final FulfillmentEventProducer fulfillmentEventProducer;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final String QR_CODE_CONTENT_KEY = "ENCORE:%s:%s";

    @Value("${services.order-service-url}")
    private String orderServiceUrl;

    private record OrderDetails(UUID userId, List<String> seatIds) {}

    public void processOrderConfirmedEvent(OrderConfirmedEvent orderConfirmedEvent) {
        OrderDetails orderDetails = getOrderDetails(orderConfirmedEvent.orderId());
        List<String> seatIds = orderDetails.seatIds();
        UUID userId = orderDetails.userId();

        if (seatIds.isEmpty() || userId == null)
            return;

        String contentKey = String.format(QR_CODE_CONTENT_KEY, orderConfirmedEvent.orderId(), String.join(",", seatIds));
        String content = generateQRCode(contentKey);
        TicketIssuedEvent ticketIssuedEvent = new TicketIssuedEvent(
                orderConfirmedEvent.orderId(),
                userId,
                seatIds,
                content
        );

        fulfillmentEventProducer.sendTicketIssuedEvent(ticketIssuedEvent, orderConfirmedEvent.orderId().toString());
        log.info("[FULFILLMENT] Ticket issued event sent for order {}", orderConfirmedEvent.orderId());
    }

    private OrderDetails getOrderDetails(UUID orderId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(orderServiceUrl + "/api/orders/" + orderId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                JsonNode seatIdsNode = json.get("seatIds");
                List<String> seatIds = new ArrayList<>();
                seatIdsNode.forEach(node -> seatIds.add(node.asText()));
                UUID userId = UUID.fromString(json.get("userId").asText());
                return new OrderDetails(userId, seatIds);
            }

            log.warn("[FULFILLMENT] Order {} not found in Order Service, status: {}", orderId, response.statusCode());
            return new OrderDetails(null, List.of());
        } catch (Exception e) {
            log.error("[FULFILLMENT] Failed to fetch seatIds for order {}: {}", orderId, e.getMessage());
            return new OrderDetails(null, List.of());
        }
    }

    private String generateQRCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            log.info("[FULFILLMENT] Generated QR code for content: {}", content);
            return base64;
        } catch (Exception e) {
            log.info("[FULFILLMENT] Failed to generate QR code for content: {} - {}", content, e.getMessage());
            return null;
        }
    }
}
