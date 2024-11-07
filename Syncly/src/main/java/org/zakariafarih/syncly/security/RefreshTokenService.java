package org.zakariafarih.syncly.security;

import org.zakariafarih.syncly.model.RefreshToken;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.RefreshTokenRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public int deleteByToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
            return 1;
        }
        return 0;
    }

    public int deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        return 1;
    }
}
