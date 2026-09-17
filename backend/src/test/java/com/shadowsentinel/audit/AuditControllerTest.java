package com.shadowsentinel.audit;

import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.shadowsentinel.TestDatabaseCleaner databaseCleaner;

    private User user;
    private User admin;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        user = userRepository.save(User.builder()
                .email("user@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        admin = userRepository.save(User.builder()
                .email("admin@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        userToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        adminToken = jwtService.generateToken(admin.getEmail(), admin.getRole().name());
    }

    @Test
    @DisplayName("GET /api/audit requires ADMIN role; regular user receives 403 Forbidden")
    void getAudit_RoleProtection() throws Exception {
        // Normal user -> 403 Forbidden
        mockMvc.perform(get("/api/audit")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Admin -> 200 OK
        mockMvc.perform(get("/api/audit")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/audit filters by eventType and userId")
    void getAudit_Filtered() throws Exception {
        auditService.log(user.getId(), AuditEventType.LOGIN, "1", "User logged in");
        auditService.log(user.getId(), AuditEventType.SESSION_STARTED, "10", "Session started");
        auditService.log(admin.getId(), AuditEventType.POLICY_CHANGED, "5", "Policy updated");

        // Filter by eventType=LOGIN
        mockMvc.perform(get("/api/audit")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("eventType", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].eventType", is("LOGIN")))
                .andExpect(jsonPath("$.content[0].details", containsString("User logged in")));

        // Filter by userId=admin.getId()
        mockMvc.perform(get("/api/audit")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("userId", admin.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].eventType", is("POLICY_CHANGED")));
    }

    @Test
    @DisplayName("AuditService.log never throws exception even if write encounters issue")
    void auditLog_NeverThrows() {
        assertDoesNotThrow(() -> {
            auditService.log(null, AuditEventType.LOGIN_FAILED, "test", "Invalid password");
        });
    }
}
