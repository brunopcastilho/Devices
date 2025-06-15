package com.bcastilho.example.webserver.domain;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record User (BigInteger id,
                    String name,
                    String creationUser,
                    LocalDateTime creationDate,
                    LocalDateTime updateDate){


    public User(BigInteger id) {
        this(id, null, null, null, null);
    }
}
