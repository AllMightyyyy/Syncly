package org.zakariafarih.syncly.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.zakariafarih.syncly.service.CustomUserDetailsService;
import org.zakariafarih.syncly.util.JwtUtil;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, @Nullable MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // For CONNECT commands, authenticate the user
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    String username = jwtUtil.extractUsername(token);
                    if (jwtUtil.isTokenValid(token, username)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                    }
                } catch (Exception e) {
                    // Optionally handle authentication failures
                }
            }
        }
        return message;
    }
}
