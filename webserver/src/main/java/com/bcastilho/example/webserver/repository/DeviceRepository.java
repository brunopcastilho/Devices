package com.bcastilho.example.webserver.repository;

import com.example.model.Device;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeviceRepository {


    Mono<Device> addDevice(Device device);

    Mono<Device> deleteDeviceById(String deviceId);

    Mono<List<Device>> findAll();

    Mono<List<Device>> findByBrand(List<String> brandName);

    Mono<List<Device>> findByState(String state);

    Mono<Device> getDeviceById(String deviceId);

    Mono<Device> updateDevice(Device device);
}