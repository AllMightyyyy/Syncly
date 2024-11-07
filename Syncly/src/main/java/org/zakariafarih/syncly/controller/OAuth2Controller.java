package org.zakariafarih.syncly.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.zakariafarih.syncly.model.DeviceType;
import org.zakariafarih.syncly.model.User;
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

// TODO -> this is a VERY SIMPLE OAUTH2 CONTROLLER, we need to handle VARIOUS SCENARIOS like duplicate usernames, store additional user information, ensure security best practices

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

    /**
     * Handles successful OAuth2 login, registers new users if necessary, and generates a JWT token.
     *
     * @param authentication the OAuth2 authentication token
     * @return a JWT token in the format "Bearer <token>"
     */
    @GetMapping("/oauth2/loginSuccess")
    public String getLoginInfo(OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String deviceInfo = "OAuth2 Device"; // Or extract from request headers or parameters

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Register new user
            user = User.builder()
                    .username(name.replaceAll(" ", "_").toLowerCase())
                    .email(email)
                    .passwordHash("") // Password not used for OAuth2 users
                    .role(User.Role.USER)
                    .build();
            userRepository.save(user);
        }

        // Register device
        deviceService.addDevice(user.getUsername(), deviceInfo, DeviceType.WEB); // Adjust DeviceType as needed

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return "Bearer " + token;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // TODO -> Token blacklist implementation
        return ResponseEntity.ok("User logged out successfully!");
    }

}