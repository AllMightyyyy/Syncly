package org.zakariafarih.syncly.controller;

import jakarta.validation.Valid;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.TwoFactorSetupResponse;
import org.zakariafarih.syncly.payload.UserProfileResponse;
import org.zakariafarih.syncly.payload.UserProfileUpdateRequest;
import org.zakariafarih.syncly.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * REST controller for managing user profiles.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves the profile of a user by username.
     *
     * @param username the username of the user
     * @return the user profile response
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserProfileResponse response = new UserProfileResponse();
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates the profile of a user.
     *
     * @param username the username of the user
     * @param request the user profile update request
     * @return the updated user profile response
     */
    @PutMapping("/{username}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable String username, @Valid @RequestBody UserProfileUpdateRequest request) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDisplayName(request.getDisplayName());
            user.setAvatarUrl(request.getAvatarUrl());
            userService.saveUser(user);
            UserProfileResponse response = new UserProfileResponse();
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setDisplayName(user.getDisplayName());
            response.setAvatarUrl(user.getAvatarUrl());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO -> Implement 2FA , Implement Account deactivation / deletion , Implement Email Verification logic right now there is only a placeholder

    @PostMapping("/{username}/deactivate")
    public ResponseEntity<?> deactivateAccount(@PathVariable String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).body("You are not authorized to deactivate this account.");
        }

        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setIsActive(false);
        userService.saveUser(user);

        return ResponseEntity.ok("Account deactivated successfully.");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAccount(@PathVariable String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).body("You are not authorized to delete this account.");
        }

        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        userService.deleteUser(user);

        return ResponseEntity.ok("Account deleted successfully.");
    }

    @PostMapping("/{username}/enable-2fa")
    public ResponseEntity<?> enableTwoFactorAuth(@PathVariable String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).body("You are not authorized to perform this action.");
        }

        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        String secret = generateSecretKey();
        user.setTwoFactorSecret(secret);
        user.setIsTwoFactorEnabled(true);
        userService.saveUser(user);

        String qrCodeUrl = getGoogleAuthenticatorBarCode(secret, user.getEmail());

        TwoFactorSetupResponse response = new TwoFactorSetupResponse(qrCodeUrl);

        return ResponseEntity.ok(response);
    }

    // Generate a random secret key
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    // Generate the QR code URL for Google Authenticator
    private String getGoogleAuthenticatorBarCode(String secretKey, String account) {
        return "otpauth://totp/"
                + URLEncoder.encode("Syncly" + ":" + account, StandardCharsets.UTF_8).replace("+", "%20")
                + "?secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8).replace("+", "%20")
                + "&issuer=" + URLEncoder.encode("Syncly", StandardCharsets.UTF_8).replace("+", "%20");
    }

    // TODO: Implement Account deactivation/deletion
    // TODO: Implement other user-related functionalities
}