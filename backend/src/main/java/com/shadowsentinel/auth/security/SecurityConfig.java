package com.shadowsentinel.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.common.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserActivityFilter userActivityFilter;
    private final DelegatedAuthenticationEntryPoint authenticationEntryPoint;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserActivityFilter userActivityFilter,
                          DelegatedAuthenticationEntryPoint authenticationEntryPoint,
                          ObjectMapper objectMapper) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userActivityFilter = userActivityFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .timestamp(Instant.now())
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                    .message("Access denied: Admin role required")
                                    .path(request.getRequestURI())
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Static resources and public authentication endpoints
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/style.css",
                                "/app.js",
                                "/favicon.ico",
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // 2. User profile endpoint (needed by frontend and extension to check role)
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                        // 3. Extension telemetry ingestion endpoints permitted for both USER and ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/sessions").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/sessions/*/end").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/activities").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/classification/evidence").hasAnyRole("USER", "ADMIN")

                        // 4. Admin employee monitoring endpoint
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")

                        // 5. Admin AI domain registry endpoints
                        .requestMatchers("/api/ai-domains/**").hasRole("ADMIN")

                        // 6. All dashboard read/admin endpoints strictly require ADMIN
                        //    (GET /api/activities, GET /api/alerts, GET /api/risk/**,
                        //     GET /api/classification/results/**, GET /api/audit/**, /api/policies/**, etc.)
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userActivityFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
