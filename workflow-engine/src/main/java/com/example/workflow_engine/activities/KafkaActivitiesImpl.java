package com.example.workflow_engine.activities;

import com.example.workflow_engine.model.KafkaMessageSpec;
import io.temporal.activity.Activity;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaActivitiesImpl implements KafkaActivities{

    private final KafkaProducer<String, String> kafkaProducer;

    public KafkaActivitiesImpl(KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void send(KafkaMessageSpec spec) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(spec.getTopic(), spec.getKey(), spec.getValueJson());
        try {
            kafkaProducer.send(record).get();
        } catch (Exception e) {
            // Let Temporal retry; ensure your messages are idempotent
            throw Activity.wrap(e);
        }
    }
}
