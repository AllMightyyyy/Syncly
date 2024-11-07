package org.zakariafarih.syncly.controller;

import jakarta.validation.Valid;
import org.jboss.aerogear.security.otp.Totp;
import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.model.DeviceType;
import org.zakariafarih.syncly.model.RefreshToken;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.*;
import org.zakariafarih.syncly.repository.DeviceRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.service.DeviceService;
import org.zakariafarih.syncly.service.EmailService;
import org.zakariafarih.syncly.service.RefreshTokenService;
import org.zakariafarih.syncly.service.UserService;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * Registers a new user.
     *
     * @param signUpRequest the signup request containing user details
     * @return a response entity indicating the result of the registration
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userService.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        String verificationToken = UUID.randomUUID().toString();

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(User.Role.USER)
                .IsEmailVerified(false)
                .emailVerificationToken(verificationToken)
                .build();

        userService.saveUser(user);

        emailService.sendEmailVerificationEmail(user.getEmail(), verificationToken);

        return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the login request containing username/email and password
     * @return a response entity containing the JWT token and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(401).body("Error: Invalid username or password");
        }

        User user = userOptional.get();

        if (!user.isIsEmailVerified()) {
            return ResponseEntity.status(403).body("Error: Email address not verified. Please check your email.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            if (user.isIsTwoFactorEnabled()) {
                // Require 2FA code verification
                return ResponseEntity.status(206).body(new TwoFactorRequiredResponse("2FA code required", user.getUsername()));
            }

            String deviceRefreshToken = UUID.randomUUID().toString();

            // Save or update the device with the new refresh token
            Device device = deviceService.addOrUpdateDevice(user.getUsername(), loginRequest.getDeviceInfo(), DeviceType.valueOf(loginRequest.getDeviceType()), deviceRefreshToken);

            JwtResponse response = new JwtResponse(jwt, deviceRefreshToken);
            response.setDeviceId(device.getId());

            return ResponseEntity.ok(response);
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

        Device device = deviceRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if the refresh token has expired
        if (device.getRefreshTokenExpiryDate() == null || device.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            device.setRefreshToken(null);
            device.setRefreshTokenExpiryDate(null);
            deviceRepository.save(device);
            return ResponseEntity.status(403).body("Refresh token has expired. Please log in again.");
        }

        // Generate new JWT token
        String token = jwtUtil.generateToken(device.getUser().getUsername());

        // Generate a new refresh token and expiry date
        String newRefreshToken = UUID.randomUUID().toString();
        device.setRefreshToken(newRefreshToken);
        device.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(30));
        deviceRepository.save(device);

        return ResponseEntity.ok(new JwtResponse(token, newRefreshToken));
    }

    /**
     * Logs out a user by deleting the refresh token.
     *
     * @param logoutRequest the logout request containing the refresh token
     * @return a response entity indicating the result of the logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        deviceService.removeRefreshToken(logoutRequest.getRefreshToken());
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
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
        userService.saveUser(user);

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
        Optional<User> userOptional = userService.findByResetPasswordToken(request.getToken());
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
        userService.saveUser(user);

        return ResponseEntity.ok("Your password has been reset successfully and you can now login!");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        Optional<User> userOptional = userService.findByEmailVerificationToken(token);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid or expired email verification token!");
        }
        User user = userOptional.get();
        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        userService.saveUser(user);

        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactorCode(@Valid @RequestBody TwoFactorVerifyRequest request) {
        Optional<User> userOptional = userService.findByUsername(request.getUsername());
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(401).body("Invalid username or 2FA code.");
        }

        User user = userOptional.get();

        if (!user.isIsTwoFactorEnabled()) {
            return ResponseEntity.badRequest().body("2FA is not enabled for this account.");
        }

        boolean isCodeValid = verifyTwoFactorCode(user.getTwoFactorSecret(), request.getCode());

        if (!isCodeValid) {
            return ResponseEntity.status(401).body("Invalid 2FA code.");
        }

        // Generate JWT token
        String jwt = jwtUtil.generateToken(user.getUsername());

        // Generate refresh token and handle device info as before
        String deviceRefreshToken = UUID.randomUUID().toString();

        // Save or update the device with the new refresh token
        Device device = deviceService.addOrUpdateDevice(user.getUsername(), request.getDeviceInfo(), request.getDeviceType(), deviceRefreshToken);

        JwtResponse response = new JwtResponse(jwt, deviceRefreshToken);
        response.setDeviceId(device.getId());

        return ResponseEntity.ok(response);
    }

    private boolean verifyTwoFactorCode(String secret, String code) {
        Totp totp = new Totp(secret);
        return totp.verify(code);
    }

}