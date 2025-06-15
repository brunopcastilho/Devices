package com.bcastilho.example.device.admin.domain;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record User(BigInteger id,
                   String name,
                   String creationUser,
                   LocalDateTime creationDate,
                   LocalDateTime updateDate) implements KafkaObject {


    public User(BigInteger id) {
        this(id, null, null, null, null);
    }


    @Override
    public String key() {
        return this.id().toString();
    }

    @Override
    public Object value() {
        return this;
    }
}
