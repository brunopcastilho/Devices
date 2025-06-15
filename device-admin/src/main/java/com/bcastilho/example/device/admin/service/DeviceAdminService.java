package com.bcastilho.example.device.admin.service;

import com.example.model.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bcastilho.example.device.admin.domain.DeviceSerdes;
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

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class DeviceAdminService {


    private final StreamsBuilderFactoryBean factoryBean;
    Logger log = LoggerFactory.getLogger(DeviceAdminService.class);
    @Value("${application.topics.device.topic-name}")
    String INPUT_TOPIC;
    ObjectMapper objectMapper;
    @Value("${application.topics.device.store-name}")
    String STORE_NAME;
    StoreBuilder<KeyValueStore<String, Device>> storeBuilder;
    KTable<String, Device> kTable;
    SelfTriggerService selfTriggerService;

    public DeviceAdminService(StoreBuilder<KeyValueStore<String, Device>> storeBuilder, StreamsBuilderFactoryBean factoryBean, SelfTriggerService selfTriggerService) {
        objectMapper = new ObjectMapper();
        this.storeBuilder = storeBuilder;
        this.factoryBean = factoryBean;
        this.selfTriggerService = selfTriggerService;
    }

    private ReadOnlyKeyValueStore<String, Device> getStore() {

        // check if kstreams is initialized
        if (kTable == null) {
            throw new IllegalStateException("KTable not initialized.");
        }

        // recover the name of  KTable
        String storeName = kTable.queryableStoreName();

        StoreQueryParameters<ReadOnlyKeyValueStore<String, Device>> storeQueryParameters = StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, Device> store = getKafkaStreams().store(storeQueryParameters);

        if (store == null) {
            throw new IllegalStateException("Store not found.");
        }

        return store;


    }

    public KafkaStreams getKafkaStreams() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new IllegalStateException("KafkaStreams ainda não foi inicializado.");
        }
        return kafkaStreams;
    }

    public StoreBuilder<KeyValueStore<String, Device>> deviceStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STORE_NAME),
                Serdes.String(),
                new DeviceSerdes()
        ).withLoggingEnabled(Collections.emptyMap());
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {


        // Build processing pipeline
        this.kTable = streamsBuilder.stream(
                        INPUT_TOPIC,
                        Consumed.with(Serdes.String(), new DeviceSerdes())
                )
                .filter((key, value) -> key != null && value != null)
                .mapValues((device) -> {

                    switch (device.getAction()) {
                        case ("addDevice"):
                            device.setAction(null);
                            device.setMessage(List.of("Created"));
                            if (device.getCreationTime() == null) {
                                device.setCreationTime(OffsetDateTime.now());
                            }
                            break;
                        case ("updateDevice"):
                            break;
                        case ("deleteDevice"):
                            device = null;
                            break;
                    }

                    return device;

                })
                .toTable(Materialized.as(STORE_NAME));


    }

    public Device getDevice(String key) {
        // Check if ktable has been initialized
        if (kTable == null) {
            throw new IllegalStateException("KTable not initialized.");
        }

        // recover the name of the state store
        String storeName = kTable.queryableStoreName();

        StoreQueryParameters<ReadOnlyKeyValueStore<String, Device>> storeQueryParameters = StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, Device> store = getKafkaStreams().store(storeQueryParameters);

        if (store == null) {
            throw new IllegalStateException("state store not found");
        }

        return store.get(key);
    }

    public List<Device> getDevices() {

        var store = getStore();

        List<Device> devices = new ArrayList<>();
        try (KeyValueIterator<String, Device> iterator = store.all()) {
            while (iterator.hasNext()) {
                devices.add(iterator.next().value);
            }
        }

        return devices;

    }

    private List<Device> getDevicesByState(String state) {

        var store = getStore();
        List<Device> devices = new ArrayList<>();
        try (KeyValueIterator<String, Device> iterator = store.all()) {
            while (iterator.hasNext()) {
                Device device = iterator.next().value;
                if (device.getBrand().equalsIgnoreCase(state))
                    devices.add(device);
            }
        }

        return devices;

    }


    public List<Device> getDevicesByBrand(List<String> brandNames) {

        var store = getStore();

        Set<String> brandSet = new HashSet<>(brandNames);

        List<Device> devices = new ArrayList<>();
        try (KeyValueIterator<String, Device> iterator = store.all()) {
            while (iterator.hasNext()) {
                Device device = iterator.next().value;
                if (brandSet.contains(device.getBrand())) {
                    devices.add(device);
                }
            }
        }

        return devices;

    }

    public Device addDevice(Device device) {

        device.setAction("addDevice");
        device.setCreationTime(OffsetDateTime.now());
        List<String> validation = validateDevice(device, null);

        if (validation.isEmpty()) {
            selfTriggerService.postObject(device.getId(), device);
        } else {
            device.setMessage(validation);
        }
        device.setState(Device.StateEnum.IN_PROCESS);
        return device;

    }

    private List<String> validateDevice(Device newDevice, Device oldDevice) {

        List<String> validations = new ArrayList<>();
        switch (newDevice.getAction()) {
            case ("updateDevice"):
                validations.addAll(validateUpdate(newDevice, oldDevice));
                break;
            case ("deleteDevice"):
                validations.addAll(validateDelete(newDevice));
                break;
        }

        return validations;

    }

    private List<String> validateDelete(Device device) {
        List<String> validations = new ArrayList<>();
        if (device.getState() == Device.StateEnum.IN_USE) {
            validations.add("Can not delete device In Use");
        }
        return validations;
    }

    private List<String> validateUpdate(Device newDevice, Device oldDevice) {
        List<String> validations = new ArrayList<>();
        if (oldDevice.getState() == Device.StateEnum.IN_USE) {
            if (!newDevice.getName().equalsIgnoreCase(oldDevice.getName())) {
                validations.add("Can not update device name when in use, try to update the status first");
            }
            if (!newDevice.getBrand().equalsIgnoreCase(oldDevice.getBrand())) {
                validations.add("Can not update device brand when in use, try to update the status first");
            }
            if (!newDevice.getCreationTime().isEqual(oldDevice.getCreationTime())) {
                validations.add("Can not update the creation time of device. System will keep the old value");
                newDevice.setCreationTime(oldDevice.getCreationTime());
            }
        }
        return validations;
    }

    public Device deleteDeviceById(String deviceId) {

        Device device = getById(deviceId);

        device.setAction("deleteDevice");
        List<String> validation = validateDevice(device, null);
        if (validation.isEmpty()) {
            selfTriggerService.postObject(device.getId(), device);
            device.setState(Device.StateEnum.IN_PROCESS);
        } else {
            device.setMessage(validation);
        }

        return device;

    }

    public List<Device> findAll() {

        return getDevices();

    }

    public List<Device> findByBrand(List<String> brandNames) {
        return getDevicesByBrand(brandNames);
    }

    public Device getById(String deviceId) {

        return getDevice(deviceId);

    }

    public Device updateDevice(Device newDevice) {

        newDevice.setAction("updateDevice");
        Device oldDevice = getById(newDevice.getId());
        List<String> validation = validateDevice(newDevice, oldDevice);

        if (validation.isEmpty()
                || (validation.size() == 1 &&
                validation.getFirst().equalsIgnoreCase("Can not update device brand when in use, try to update the status first"))) {
            selfTriggerService.postObject(newDevice.getId(), newDevice);
            newDevice.setState(Device.StateEnum.IN_PROCESS);
        } else {
            newDevice.setMessage(validation);
        }

        return newDevice;

    }

    public List<Device> findByState(String state) {
        return getDevicesByState(state);
    }


}

