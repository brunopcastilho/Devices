package com.bcastilho.example.device.admin.repository;

import com.example.model.Device;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaRepository {

    String topic;
    KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaRepository(KafkaTemplate<String, Object> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Device publishObject(Device obj) {
        kafkaTemplate.send(topic, obj.getId(), obj);
        return obj;

    }

    public Device getObject(String key) {

        // get from KafkaStateStore
        return null;

    }
}
