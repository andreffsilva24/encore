package com.encore.fulfillment_service.kafka.producer;

import com.encore.fulfillment_service.kafka.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentEventProducer {


    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendTicketIssuedEvent(Object event, String key) {
        publish(Topics.TICKET_ISSUED, key, event);
    }

    private void publish(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish to {} with key {}: {}", topic, key, ex.getMessage());
                        } else {
                            log.info("Published to {} [partition {} @ offset {}]",
                                    topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for topic {}: {}", topic, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
