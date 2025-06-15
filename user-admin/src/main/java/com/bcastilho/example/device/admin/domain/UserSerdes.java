package com.bcastilho.example.device.admin.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serde;

public class UserSerdes implements Serde<User> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Override
    public UserSerializer serializer() {
        return new UserSerializer();
    }

    @Override
    public UserDeserializer deserializer() {
        return new UserDeserializer();
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
