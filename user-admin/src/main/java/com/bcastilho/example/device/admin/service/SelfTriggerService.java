package com.bcastilho.example.device.admin.service;

import com.bcastilho.example.device.admin.domain.KafkaObject;
import com.bcastilho.example.device.admin.repository.KafkaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SelfTriggerService<T extends KafkaObject> {
    /*This is a generic service that receives an object from the controller and posts it to the parametrized kafka topic */


    KafkaRepository<T> repository;

    public SelfTriggerService(KafkaTemplate<String, Object> template, @Qualifier("topic-name") String topicName) {
        repository = new KafkaRepository<T>(template, topicName);

    }

    public void postObject(String key, T object) {
        repository.publishObject(object);
    }


}
