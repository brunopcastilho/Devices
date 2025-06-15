package com.bcastilho.example.webserver.repository;


import com.example.model.Device;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class DeviceRepositoryImpl implements DeviceRepository {

    private final WebClient webClient;

    public DeviceRepositoryImpl(@Qualifier("deviceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }


    @Override
    public Mono<Device> addDevice(Device device) {
        return webClient.post()
                .uri("/device")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(device)
                .retrieve()
                .bodyToMono(Device.class);
    }

    @Override
    public Mono<Device> deleteDeviceById(String deviceId) {
        return webClient.delete()
                .uri("/device/{deviceId}", deviceId)
                .retrieve()
                .bodyToMono(Device.class);
    }

    @Override
    public Mono<List<Device>> findAll() {
        return webClient.get()
                .uri("/device/findAll")
                .retrieve()
                .bodyToFlux(Device.class)
                .collectList();
    }

    @Override
    public Mono<List<Device>> findByBrand(List<String> brandName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/device/findByBrand/{brandName}")
                        .build(String.join(",", brandName)))
                .retrieve()
                .bodyToFlux(Device.class)
                .collectList();
    }

    @Override
    public Mono<List<Device>> findByState(String state) {
        return webClient.get()
                .uri("/device/findByState/{state}", state)
                .retrieve()
                .bodyToFlux(Device.class)
                .collectList();
    }

    @Override
    public Mono<Device> getDeviceById(String deviceId) {
        return webClient.get()
                .uri("/device/{deviceId}", deviceId)
                .retrieve()
                .bodyToMono(Device.class);
    }

    public Mono<Device> updateDevice(Device device) {
        return webClient.put()
                .uri("/device")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(device)
                .retrieve()
                .bodyToMono(Device.class);
    }
}