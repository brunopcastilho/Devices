package com.bcastilho.example.device.admin.service;

import com.bcastilho.example.device.admin.domain.User;
import com.bcastilho.example.device.admin.domain.UserSerdes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserAdminService {

    private final StreamsBuilderFactoryBean factoryBean;
    Logger log = LoggerFactory.getLogger(UserAdminService.class);
    @Value("${application.topics.user.topic-name}")
    String INPUT_TOPIC;
    ObjectMapper objectMapper;
    @Value("${application.topics.user.store-name}")
    String STORE_NAME;
    StoreBuilder<KeyValueStore<String, User>> storeBuilder;
    KTable<String, User> kTable;
    SelfTriggerService<User> selfTriggerService;

    public UserAdminService(StoreBuilder<KeyValueStore<String, User>> storeBuilder, StreamsBuilderFactoryBean factoryBean, SelfTriggerService<User> selfTriggerService) {
        objectMapper = new ObjectMapper();
        this.storeBuilder = storeBuilder;
        this.factoryBean = factoryBean;
        this.selfTriggerService = selfTriggerService;
    }

    public KafkaStreams getKafkaStreams() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new IllegalStateException("KafkaStreams ainda não foi inicializado.");
        }
        return kafkaStreams;
    }

    public StoreBuilder<KeyValueStore<String, User>> userStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STORE_NAME),
                Serdes.String(),
                new UserSerdes()
        ).withLoggingEnabled(Collections.emptyMap());
    }


    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {


        // Build processing pipeline
        this.kTable = streamsBuilder.stream(
                        INPUT_TOPIC,
                        Consumed.with(Serdes.String(), new UserSerdes())
                )
                .filter((key, value) -> key != null && value != null)
                .toTable(Materialized.as(STORE_NAME));


    }

    public User getUser(String key) {
        // Verifica se o KafkaStreams está inicializado
        if (kTable == null) {
            throw new IllegalStateException("KTable não foi inicializada.");
        }

        // Recupera o nome da store associada à KTable
        String storeName = kTable.queryableStoreName();

        StoreQueryParameters<ReadOnlyKeyValueStore<String, User>> storeQueryParameters = StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, User> store = getKafkaStreams().store(storeQueryParameters);

        if (store == null) {
            throw new IllegalStateException("Store não foi encontrada.");
        }

        return store.get(key);
    }


    public User createUser(User user) {

        selfTriggerService.postObject(user.key(), (User) user.value());
        return user;
    }


    private User parseAndValidate(User value) {
        try {


            // Perform validation
            if (value.id() == null) {
                log.error("Invalid User object: {}", value);
                return null;
            }
            return value;
        } catch (Exception e) {
            log.error("Failed to parse or validate message: {}", value, e);
            return null;
        }
    }


    public List<User> getUsers() {
        // Verifica se o KafkaStreams está inicializado
        if (kTable == null) {
            throw new IllegalStateException("KTable não foi inicializada.");
        }

        // Recupera o nome da store associada à KTable
        String storeName = kTable.queryableStoreName();

        StoreQueryParameters<ReadOnlyKeyValueStore<String, User>> storeQueryParameters = StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, User> store = getKafkaStreams().store(storeQueryParameters);

        if (store == null) {
            throw new IllegalStateException("Store não foi encontrada.");
        }

        List<User> users = new ArrayList<>();
        try (KeyValueIterator<String, User> iterator = store.all()) {
            while (iterator.hasNext()) {
                users.add(iterator.next().value);
            }
        }

        return users;

    }
}

