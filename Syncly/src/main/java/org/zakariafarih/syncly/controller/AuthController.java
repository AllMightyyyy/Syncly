package org.zakariafarih.syncly.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.jboss.aerogear.security.otp.Totp;
import org.zakariafarih.syncly.model.*;
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

        // Add to password history
        PasswordHistory passwordHistory = PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPasswordHash())
                .build();
        userService.savePasswordHistory(passwordHistory);

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
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

            String refreshToken = UUID.randomUUID().toString();

            // Save or update the device with the new refresh token
            Device device = deviceService.addOrUpdateDevice(user.getUsername(), loginRequest.getDeviceInfo(), DeviceType.valueOf(loginRequest.getDeviceType()), refreshToken);

            // Set refresh token in HttpOnly cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true); // Set to true in production
            refreshTokenCookie.setPath("/api/v1/auth/refresh-token");
            refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(refreshTokenCookie);

            // Manually set SameSite attribute in the Set-Cookie header
            response.addHeader("Set-Cookie", "refreshToken=" + refreshToken +
                    "; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh-token; Max-Age=" + (30 * 24 * 60 * 60));

            JwtResponse jwtResponse = new JwtResponse(jwt, null); // Remove refreshToken from response body
            jwtResponse.setDeviceId(device.getId());

            return ResponseEntity.ok(jwtResponse);
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
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // Retrieve refresh token from cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(403).body("Refresh token is missing");
        }

        try {
            Device device = deviceService.validateRefreshToken(refreshToken);

            // Generate new JWT token
            String token = jwtUtil.generateToken(device.getUser().getUsername());

            // Generate a new refresh token and set it in cookie
            String newRefreshToken = UUID.randomUUID().toString();
            device.setRefreshToken(newRefreshToken);
            device.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(30));
            deviceRepository.save(device);

            // Set new refresh token in HttpOnly cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true); // Set to true in production
            refreshTokenCookie.setPath("/api/v1/auth/refresh-token");
            refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(refreshTokenCookie);

            // Manually set SameSite attribute in the Set-Cookie header
            response.addHeader("Set-Cookie", "refreshToken=" + newRefreshToken +
                    "; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh-token; Max-Age=" + (30 * 24 * 60 * 60));

            JwtResponse jwtResponse = new JwtResponse(token, null); // Remove refreshToken from response body

            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        // Clear the refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Set to true in production
        refreshTokenCookie.setPath("/api/v1/auth/refresh-token");
        refreshTokenCookie.setMaxAge(0); // Delete the cookie
        response.addCookie(refreshTokenCookie);

        // Manually set SameSite attribute in the Set-Cookie header to ensure it is deleted with the same attributes
        response.addHeader("Set-Cookie", "refreshToken=; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh-token; Max-Age=0");

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
            return ResponseEntity.badRequest().body("That password reset link is Invalid or Expired.");
        }

        User user = userOptional.get();
        if (user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("That password reset link has expired.");
        }

        try {
            userService.changePassword(user.getUsername(), request.getNewPassword());
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiresAt(null);
            userService.saveUser(user);

            return ResponseEntity.ok("Your password has been reset successfully and you can now login!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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