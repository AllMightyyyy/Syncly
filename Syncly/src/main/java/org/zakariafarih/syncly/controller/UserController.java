package org.zakariafarih.syncly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.UserProfileResponse;
import org.zakariafarih.syncly.payload.UserProfileUpdateRequest;
import org.zakariafarih.syncly.repository.UserRepository;

import java.util.Optional;

/**
 * REST controller for managing user profiles.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the profile of a user by username.
     *
     * @param username the username of the user
     * @return the user profile response
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
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
    public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable String username, @RequestBody UserProfileUpdateRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDisplayName(request.getDisplayName());
            user.setAvatarUrl(request.getAvatarUrl());
            userRepository.save(user);
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
}