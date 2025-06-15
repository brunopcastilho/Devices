package com.bcastilho.example.device.admin.configuration;

import com.bcastilho.example.device.admin.domain.User;
import com.bcastilho.example.device.admin.domain.UserSerdes;
import com.bcastilho.example.device.admin.domain.UserSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaConfiguration {

    @Value("${application.topics.user.topic-name}") // This is the topic name for the Kafka producer
    public String topicName; // This is the topic name for the Kafka producer


    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers; // Kafka server address
    @Value("${spring.application.name}")
    String applicationId;
    @Value("${spring.kafka.streams.replication-factor}")
    int replicationFactor;
    @Value("${application.topics.user.store-name}")
    String STORE_NAME;

    @Bean
    @Qualifier("topic-name")
    public String topicName() {
        return this.topicName;
    }


    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka server address
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserSerializer.class);

        // Optional producer configurations (recommended for production)
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all in-sync replicas to acknowledge
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Number of retries for failed requests
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Ensure exactly once delivery

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // KafkaTemplate wraps the producer and provides convenience methods
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();

        // Required configurations
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Default serializers/deserializers
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, UserSerdes.class.getName());

        // Performance and reliability configurations
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, replicationFactor);

        // Recommended production settings
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG), "true");
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG), "500");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100); // Flush more frequently than default


        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StoreBuilder<KeyValueStore<String, User>> storeBuilder() {
        return Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(STORE_NAME),
                        Serdes.String(),
                        new UserSerdes()
                )
                .withLoggingEnabled(Map.of()); // This enables the changelog topic
    }

}


