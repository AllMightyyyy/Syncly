package org.zakariafarih.syncly.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.zakariafarih.syncly.model.Device;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.DeviceRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.util.JwtUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        String jwt = jwtUtil.generateToken(username);

        // Generate a new refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Save the refresh token with device info (assuming deviceName is sent as a request parameter)
        String deviceName = request.getParameter("deviceInfo");
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = "Unknown Device";
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Device device = deviceRepository.findByUserAndDeviceName(user, deviceName)
                .orElse(Device.builder()
                        .user(user)
                        .deviceName(deviceName)
                        .build());

        device.setRefreshToken(refreshToken);
        device.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(30));
        deviceRepository.save(device);

        // Set refresh token in HttpOnly cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Set to true in production
        refreshTokenCookie.setPath("/api/v1/auth/refresh-token");
        refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(refreshTokenCookie);

        // Manually add SameSite attribute to the Set-Cookie header
        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken +
                "; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh-token; Max-Age=" + (30 * 24 * 60 * 60));

        // Respond with JWT token
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + jwt + "\"}");
    }
}