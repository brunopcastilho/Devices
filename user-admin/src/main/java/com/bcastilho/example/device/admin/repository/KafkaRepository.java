package com.bcastilho.example.device.admin.repository;

import com.bcastilho.example.device.admin.domain.KafkaObject;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaRepository<T extends KafkaObject> {

    String topic;
    KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaRepository(KafkaTemplate<String, Object> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public T publishObject(T obj) {
        kafkaTemplate.send(topic, obj.key(), obj.value());
        return obj;

    }

    public T getObject(String key) {

        // get from KafkaStateStore
        return null;

    }
}
