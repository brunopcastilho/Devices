package com.bcastilho.example.device.admin.domain;

import com.example.model.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class DeviceSerializer implements Serializer<Device> {

    ObjectMapper objectMapper;

    public DeviceSerializer() {

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ;
    }


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No configuration needed
    }

    @Override
    public byte[] serialize(String topic, Device device) {
        try {
            if (device == null) {
                return null;
            }
            // Convert User object to JSON string and then to byte array
            return objectMapper.writeValueAsString(device).getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error serializing User to string", e);
        }
    }

    @Override
    public void close() {
        // Nothing to close
    }
}