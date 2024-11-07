package org.zakariafarih.syncly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zakariafarih.syncly.model.RefreshToken;
import org.zakariafarih.syncly.repository.RefreshTokenRepository;

import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public int deleteByToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
            return 1;
        }
        return 0;
    }

}
