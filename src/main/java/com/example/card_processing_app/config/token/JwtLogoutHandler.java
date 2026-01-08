package com.example.card_processing_app.config.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class JwtLogoutHandler implements LogoutHandler {

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout attempt without a valid Bearer token");
            return;
        }

        final String jwt = authHeader.substring(7);

        log.info("Cleaning security context for logout...");
        SecurityContextHolder.clearContext();

        log.info("User successfully logged out.");
    }
}
