package com.bcastilho.example.device.admin.api;

import com.example.api.DeviceApi;
import com.example.model.Device;
import com.bcastilho.example.device.admin.service.DeviceAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeviceController implements DeviceApi {


    DeviceAdminService deviceAdminService;


    public DeviceController(DeviceAdminService deviceAdminService) {
        this.deviceAdminService = deviceAdminService;

    }

    @Override
    public ResponseEntity<Device> deleteDeviceById(String deviceId) {

        Device device = deviceAdminService.deleteDeviceById(deviceId);
        if (device.getState() == Device.StateEnum.IN_PROCESS) {
            return new ResponseEntity<>(device, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(device, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<Device> addDevice(Device device) {

        Device retDevice = deviceAdminService.addDevice(device);

        return new ResponseEntity<>(retDevice, HttpStatus.ACCEPTED);

    }


    @Override
    public ResponseEntity<List<Device>> findAll() {
        List<Device> devices = deviceAdminService.findAll();
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Device>> findByBrand(List<String> brandNames) {
        List<Device> devices = deviceAdminService.findByBrand(brandNames);
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Device> getDeviceById(String deviceId) {
        Device device = deviceAdminService.getById(deviceId);
        if (device != null) {
            return new ResponseEntity<>(device, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

    }

    @Override
    public ResponseEntity<Device> updateDevice(Device device) {

        Device retDevice = deviceAdminService.updateDevice(device);
        return new ResponseEntity<>(retDevice, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Device>> findByState(String state) {
        List<Device> devices = deviceAdminService.findByState(state);
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }
}
