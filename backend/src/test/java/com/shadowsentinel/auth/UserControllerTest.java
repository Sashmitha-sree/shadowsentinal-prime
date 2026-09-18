package com.shadowsentinel.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.TestDatabaseCleaner;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private User adminUser;
    private User onlineEmployee;
    private User offlineEmployee;
    private User neverSeenEmployee;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        adminUser = userRepository.save(User.builder()
                .email("admin@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        // Active 1 minute ago -> Online
        onlineEmployee = userRepository.save(User.builder()
                .email("online.worker@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .lastSeenAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build());

        // Active 15 minutes ago -> Offline
        offlineEmployee = userRepository.save(User.builder()
                .email("offline.worker@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .lastSeenAt(Instant.now().minus(15, ChronoUnit.MINUTES))
                .build());

        // Never active (null lastSeenAt) -> Offline
        neverSeenEmployee = userRepository.save(User.builder()
                .email("never.worker@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .lastSeenAt(null)
                .build());

        adminToken = jwtService.generateToken(adminUser.getEmail(), adminUser.getRole().name());
        employeeToken = jwtService.generateToken(onlineEmployee.getEmail(), onlineEmployee.getRole().name());
    }

    @Test
    @DisplayName("GET /api/users as ADMIN returns 200 OK with all USER-role accounts and online status")
    void getUsers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // Online employee sorted to top
                .andExpect(jsonPath("$[0].email", is("online.worker@sentinel.local")))
                .andExpect(jsonPath("$[0].role", is("USER")))
                .andExpect(jsonPath("$[0].online", is(true)))
                .andExpect(jsonPath("$[0].lastSeenAt", notNullValue()))
                // Offline employee next (has lastSeenAt but > 5m ago)
                .andExpect(jsonPath("$[1].email", is("offline.worker@sentinel.local")))
                .andExpect(jsonPath("$[1].role", is("USER")))
                .andExpect(jsonPath("$[1].online", is(false)))
                .andExpect(jsonPath("$[1].lastSeenAt", notNullValue()))
                // Never-seen employee last
                .andExpect(jsonPath("$[2].email", is("never.worker@sentinel.local")))
                .andExpect(jsonPath("$[2].role", is("USER")))
                .andExpect(jsonPath("$[2].online", is(false)))
                .andExpect(jsonPath("$[2].lastSeenAt", nullValue()));
    }

    @Test
    @DisplayName("GET /api/users as ADMIN excludes accounts with ADMIN role")
    void getUsers_ExcludesAdmins() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", not(hasItem("admin@sentinel.local"))))
                .andExpect(jsonPath("$[*].role", not(hasItem("ADMIN"))));
    }

    @Test
    @DisplayName("GET /api/users as USER returns 403 Forbidden")
    void getUsers_AsUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Admin role required")));
    }

    @Test
    @DisplayName("GET /api/users unauthenticated returns 401 Unauthorized")
    void getUsers_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
