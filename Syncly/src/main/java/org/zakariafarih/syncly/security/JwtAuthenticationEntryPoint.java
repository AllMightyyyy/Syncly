package org.zakariafarih.syncly.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JwtAuthenticationEntryPoint is an implementation of AuthenticationEntryPoint
 * that rejects unauthorized requests with an HTTP 401 Unauthorized error.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Commences an authentication scheme.
     * <p>
     * This method is called when an unauthenticated user tries to access a secured resource.
     * It sends a 401 Unauthorized response.
     *
     * @param request       the HTTP request
     * @param response      the HTTP response
     * @param authException the exception that caused the invocation
     * @throws IOException      if an input or output error occurs
     * @throws jakarta.servlet.ServletException if a servlet error occurs
     */
    @Override
    public void commence(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, AuthenticationException authException) throws IOException, jakarta.servlet.ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}