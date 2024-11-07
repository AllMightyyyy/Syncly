package org.zakariafarih.syncly.service;

import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.model.DeviceType;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.DeviceRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing user devices.
 */
@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the list of devices for a given user.
     *
     * @param username the username of the user
     * @return a list of devices associated with the user
     */
    public List<Device> getUserDevices(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return deviceRepository.findByUser(user);
    }

    /**
     * Adds a new device for a given user.
     *
     * @param username the username of the user
     * @param deviceName the name of the device
     * @param deviceType the type of the device
     * @return the added device
     */
    public Device addDevice(String username, String deviceName, DeviceType deviceType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Device device = Device.builder()
                .user(user)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .build();
        return deviceRepository.save(device);
    }

    /**
     * Removes a device for a given user.
     *
     * @param username the username of the user
     * @param deviceId the ID of the device to remove
     */
    public void removeDevice(String username, Long deviceId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        if (!device.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to remove this device");
        }
        deviceRepository.delete(device);
    }
}