package org.zakariafarih.syncly.controller;

import jakarta.validation.Valid;
import org.zakariafarih.syncly.model.RefreshToken;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.*;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.service.EmailService;
import org.zakariafarih.syncly.service.RefreshTokenService;
import org.zakariafarih.syncly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for handling authentication-related requests.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    /**
     * Registers a new user.
     *
     * @param signUpRequest the signup request containing user details
     * @return a response entity indicating the result of the registration
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the login request containing username/email and password
     * @return a response entity containing the JWT token and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Error: Invalid username or password");
        }
    }

    /**
     * Refreshes the JWT token using a refresh token.
     *
     * @param request the token refresh request containing the refresh token
     * @return a response entity containing the new JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getUsername());
                    return ResponseEntity.ok(new JwtResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    /**
     * Logs out a user by deleting the refresh token.
     *
     * @param logoutRequest the logout request containing the refresh token
     * @return a response entity indicating the result of the logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        refreshTokenService.deleteByToken(logoutRequest.getRefreshToken());
        return ResponseEntity.ok("User logged out successfully!");
    }

    /**
     * Requests a password reset by sending a reset link to the user's email.
     *
     * @param request the password reset request containing the user's email
     * @return a response entity indicating the result of the request
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok("If that email address is in our system, we have sent a password reset link to it.");
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return ResponseEntity.ok("If that email address is in our system, we have sent a password reset link to it.");
    }

    /**
     * Resets the user's password using a valid reset token.
     *
     * @param request the set new password request containing the reset token and new password
     * @return a response entity indicating the result of the password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody SetNewPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(request.getToken());
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok("That password reset link is Invalid or Expired.");
        }

        User user = userOptional.get();
        if (user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok("That password reset link has expired.");
        }

        // Update user's password since it's a valid token
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        userRepository.save(user);

        return ResponseEntity.ok("Your password has been reset successfully and you can now login!");
    }
}