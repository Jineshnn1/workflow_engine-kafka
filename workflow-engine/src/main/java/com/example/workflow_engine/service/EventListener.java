package com.example.workflow_engine.service;

import com.example.workflow_engine.dao.EventEntity;
import com.example.workflow_engine.dao.EventRepository;
import com.example.workflow_engine.model.EventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventRepository repository;

    @Value("${kafka.topic:my-topic}")
    private String topic;

    public EventListener(EventRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${kafka.topic:my-topic}",
            groupId = "order-events-consumer")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            String value = record.value();
            log.info("Received Kafka message: {}", value);

            EventMessage msg =
                    objectMapper.readValue(value, EventMessage.class);

            EventEntity entity = new EventEntity(
                    msg.getEvent(),
                    msg.getOrderId()
            );

            repository.save(entity);
            log.info("Saved OrderEvent to DB: {}", entity.getId());

        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
            // Optionally let the exception bubble to trigger retries/DLQ, depending on your config
        }
    }

}
