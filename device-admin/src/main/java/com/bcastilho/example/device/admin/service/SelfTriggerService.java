package com.bcastilho.example.device.admin.service;

import com.example.model.Device;
import com.bcastilho.example.device.admin.repository.KafkaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SelfTriggerService {


    KafkaRepository repository;

    public SelfTriggerService(KafkaTemplate<String, Object> template, @Qualifier("topic-name") String topicName) {
        repository = new KafkaRepository(template, topicName);

    }

    public void postObject(String key, Device object) {
        repository.publishObject(object);
    }


}
