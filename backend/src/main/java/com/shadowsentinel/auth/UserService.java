package com.shadowsentinel.auth;

import com.shadowsentinel.auth.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateLastSeenAt(Long userId, Instant timestamp) {
        if (userId != null && timestamp != null) {
            userRepository.updateLastSeenAt(userId, timestamp);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getEmployees() {
        return getEmployees(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getEmployees(Instant now) {
        List<User> users = userRepository.findByRole(Role.USER);
        Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);

        return users.stream()
                .map(user -> {
                    boolean online = user.getLastSeenAt() != null && !user.getLastSeenAt().isBefore(fiveMinutesAgo);
                    return UserResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .companyId(user.getCompanyId())
                            .createdAt(user.getCreatedAt())
                            .lastSeenAt(user.getLastSeenAt())
                            .online(online)
                            .build();
                })
                .sorted(Comparator
                        .comparing(UserResponse::getOnline, Comparator.reverseOrder())
                        .thenComparing(UserResponse::getLastSeenAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(UserResponse::getEmail, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
