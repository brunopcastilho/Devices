package com.bcastilho.example.webserver.service;

import com.example.model.Device;
import com.bcastilho.example.webserver.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {
    DeviceRepository repository;


    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    public Device addDevice(Device device) {
        return this.repository.addDevice(device).block();
    }

    public Device deleteDeviceById(String deviceId) {
        return repository.deleteDeviceById(deviceId).block();
    }

    public List<Device> findAll() {
        return repository.findAll().block();
    }

    public List<Device> findByBrand(List<String> brandNames) {
        return repository.findByBrand(brandNames).block();
    }

    public List<Device> findByState(String state) {
        return repository.findByState(state).block();
    }

    public Device getDeviceById(String deviceId) {
        return repository.getDeviceById(deviceId).block();
    }

    public Device updateDevice(Device device) {
        return repository.updateDevice(device).block();
    }
}
