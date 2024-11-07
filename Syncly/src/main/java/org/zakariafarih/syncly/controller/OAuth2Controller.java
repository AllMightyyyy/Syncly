package org.zakariafarih.syncly.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.zakariafarih.syncly.model.DeviceType;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.JwtResponse;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.service.DeviceService;
import org.zakariafarih.syncly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller for handling OAuth2 authentication and generating JWT tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/oauth2/loginSuccess")
    public ResponseEntity<?> getLoginInfo(OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String deviceInfo = "OAuth2 Device"; // Or extract from request headers or parameters

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            String username = generateUniqueUsername(name);
            user = User.builder()
                    .username(username)
                    .email(email)
                    .role(User.Role.USER)
                    .build();
            userRepository.save(user);
        }

        // Register device
        deviceService.addDevice(user.getUsername(), deviceInfo, DeviceType.WEB);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(new JwtResponse(token, null));
    }

    private String generateUniqueUsername(String name) {
        String baseUsername = name.replaceAll(" ", "_").toLowerCase();
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Implement token revocation if necessary
        return ResponseEntity.ok("User logged out successfully!");
    }
}
