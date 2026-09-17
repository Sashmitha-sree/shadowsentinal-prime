package com.shadowsentinel.auth;

import com.shadowsentinel.auth.dto.AuthResponse;
import com.shadowsentinel.auth.dto.LoginRequest;
import com.shadowsentinel.auth.dto.RegisterRequest;
import com.shadowsentinel.auth.dto.UserResponse;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.common.UserAlreadyExistsException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.shadowsentinel.audit.AuditService auditService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, com.shadowsentinel.audit.AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("User with email '" + normalizedEmail + "' already exists");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .companyId(request.getCompanyId())
                .build();

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        java.util.Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            auditService.log(userOpt.map(User::getId).orElse(null), com.shadowsentinel.audit.AuditEventType.LOGIN_FAILED,
                    normalizedEmail, "Invalid email or password");
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        Instant expiresAt = jwtService.calculateExpiration();

        auditService.log(user.getId(), com.shadowsentinel.audit.AuditEventType.LOGIN,
                user.getId().toString(), "User login successful");

        return AuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BadCredentialsException("Unauthorized - User not authenticated");
        }

        User refreshedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(refreshedUser);
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
