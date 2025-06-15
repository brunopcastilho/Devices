package com.bcastilho.example.webserver.api;


import com.bcastilho.example.webserver.service.DeviceService;
import com.example.model.Device;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DeviceControllerGraphql {

    DeviceService service;

    public DeviceControllerGraphql(DeviceService service) {
        this.service = service;
    }

    @QueryMapping
    public Device getDevice(@Argument String id) {
        return service.getDeviceById(id);
    }

    @QueryMapping
    public List<Device> getDevices() {
        return service.findAll();
    }

}
