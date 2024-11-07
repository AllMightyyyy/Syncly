package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUser(User user);
    Optional<Device> findByUserAndDeviceName(User user, String deviceName);
    Optional<Device> findByRefreshToken(String refreshToken);
}
