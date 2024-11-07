package org.zakariafarih.syncly.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUser(User user);
    Optional<Device> findByUserAndDeviceName(User user, String deviceName);
    Optional<Device> findByRefreshToken(String refreshToken);
    @Modifying
    @Query("UPDATE Device d SET d.refreshToken = null, d.refreshTokenExpiryDate = null WHERE d.user.id = :userId")
    void removeAllRefreshTokensByUser(@Param("userId") Long userId);
}
