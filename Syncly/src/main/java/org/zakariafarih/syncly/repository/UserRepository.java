package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Optional<User> findByUsernameOrEmail(String username, String email);
}
