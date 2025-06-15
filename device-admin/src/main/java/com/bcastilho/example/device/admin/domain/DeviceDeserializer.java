package com.bcastilho.example.device.admin.domain;

import com.example.model.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class DeviceDeserializer implements Deserializer<Device> {

    ObjectMapper objectMapper;

    public DeviceDeserializer() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No configuration needed
    }

    @Override
    public Device deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            // Convert byte array to String and then to User object
            return objectMapper.readValue(new String(data, "UTF-8"), Device.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing User from string", e);
        }
    }

    @Override
    public void close() {
        // Nothing to close
    }
}