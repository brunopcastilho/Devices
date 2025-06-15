package com.bcastilho.example.webserver.api;

import com.example.api.DeviceApi;
import com.example.model.Device;
import com.bcastilho.example.webserver.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeviceController implements DeviceApi {

    DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }


    @Override
    public ResponseEntity<Device> addDevice(Device device) {
        try {
            service.addDevice(device);
            return new ResponseEntity<>(device, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<Device> deleteDeviceById(String deviceId) {
        try {
            Device device = service.deleteDeviceById(deviceId);
            return new ResponseEntity<>(device, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Device>> findAll() {
        try {
            List<Device> devices = service.findAll();
            return new ResponseEntity<>(devices, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Device>> findByBrand(List<String> brandName) {
        try {
            List<Device> devices = service.findByBrand(brandName);
            return new ResponseEntity<>(devices, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Device>> findByState(String state) {
        try {
            List<Device> devices = service.findByState(state);
            return new ResponseEntity<>(devices, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Device> getDeviceById(String deviceId) {
        try {
            Device device = service.getDeviceById(deviceId);
            return new ResponseEntity<>(device, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Device> updateDevice(Device device) {
        try {
            Device returnDevice = service.updateDevice(device);
            return new ResponseEntity<>(returnDevice, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
