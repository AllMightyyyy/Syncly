package org.zakariafarih.syncly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zakariafarih.syncly.exception.UserNotFoundException;
import org.zakariafarih.syncly.model.PasswordHistory;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.DeviceRepository;
import org.zakariafarih.syncly.repository.PasswordHistoryRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .filter(user -> user.getUsername() != null && user.getEmail() != null); // Ensure non-null username/email
    }

    public Optional<User> findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findByEmailVerificationToken(String token) {
        return userRepository.findByEmailVerificationToken(token);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the new password matches any in history
        List<PasswordHistory> recentPasswords = passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user);
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new RuntimeException("You cannot reuse any of your last 5 passwords.");
            }
        }

        // Update user's password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);

        // Add to password history
        PasswordHistory passwordHistory = PasswordHistory.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .build();
        passwordHistoryRepository.save(passwordHistory);

        // Remove oldest password history if exceeding limit
        if (recentPasswords.size() >= 5) {
            PasswordHistory oldest = recentPasswords.get(recentPasswords.size() - 1);
            passwordHistoryRepository.delete(oldest);
        }

        // Invalidate all refresh tokens
        deviceRepository.removeAllRefreshTokensByUser(user.getId());
    }

    public void savePasswordHistory(PasswordHistory passwordHistory) {
        passwordHistoryRepository.save(passwordHistory);
    }
}
