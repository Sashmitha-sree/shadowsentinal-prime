package com.shadowsentinel.auth.security;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class UserActivityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserActivityFilter.class);

    private final UserService userService;

    public UserActivityFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String uri = request.getRequestURI();
            String servletPath = request.getServletPath();

            boolean isTarget = (uri != null && (uri.startsWith("/api/sessions") || uri.startsWith("/api/activities")))
                    || (servletPath != null && (servletPath.startsWith("/api/sessions") || servletPath.startsWith("/api/activities")));

            if (isTarget) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {
                    Instant now = Instant.now();
                    user.setLastSeenAt(now);
                    userService.updateLastSeenAt(user.getId(), now);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update user lastSeenAt timestamp: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
