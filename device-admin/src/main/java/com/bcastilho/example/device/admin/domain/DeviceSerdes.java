package com.bcastilho.example.device.admin.domain;

import com.example.model.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serde;

public class DeviceSerdes implements Serde<Device> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Override
    public DeviceSerializer serializer() {
        return new DeviceSerializer();
    }

    @Override
    public DeviceDeserializer deserializer() {
        return new DeviceDeserializer();
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
