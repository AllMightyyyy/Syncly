package org.zakariafarih.syncly.controller;

import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.payload.DeviceAddRequest;
import org.zakariafarih.syncly.payload.DeviceDeleteResponse;
import org.zakariafarih.syncly.payload.DeviceResponse;
import org.zakariafarih.syncly.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/add")
    public ResponseEntity<?> addDevice(@RequestBody DeviceAddRequest request, Authentication authentication) {
        String username = authentication.getName();
        Device device = deviceService.addDevice(username, request.getDeviceName(), request.getDeviceType());
        return ResponseEntity.ok("Device added successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDevices(Authentication authentication) {
        String username = authentication.getName();
        List<Device> devices = deviceService.getUserDevices(username);

        List<DeviceResponse> responses = devices.stream().map(device -> {
            DeviceResponse response = new DeviceResponse();
            response.setId(device.getId());
            response.setDeviceName(device.getDeviceName());
            response.setDeviceType(device.getDeviceType());
            response.setCreatedAt(device.getCreatedAt().toString());
            return response;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        deviceService.removeDevice(username, id);
        DeviceDeleteResponse response = new DeviceDeleteResponse();
        response.setMessage("Device removed successfully");
        return ResponseEntity.ok(response);
    }
}
