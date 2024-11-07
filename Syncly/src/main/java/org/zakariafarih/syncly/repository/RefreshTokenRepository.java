package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zakariafarih.syncly.model.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
