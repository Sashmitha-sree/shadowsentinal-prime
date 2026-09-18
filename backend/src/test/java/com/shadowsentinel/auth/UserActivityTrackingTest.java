package com.shadowsentinel.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.TestDatabaseCleaner;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.browser.BrowserSession;
import com.shadowsentinel.browser.BrowserSessionRepository;
import com.shadowsentinel.browser.SessionStatus;
import com.shadowsentinel.browser.dto.CreateActivityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserActivityTrackingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrowserSessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private User employee;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        employee = userRepository.save(User.builder()
                .email("worker@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .lastSeenAt(null)
                .build());

        employeeToken = jwtService.generateToken(employee.getEmail(), employee.getRole().name());
    }

    @Test
    @DisplayName("POST /api/sessions updates user lastSeenAt timestamp")
    void createSession_UpdatesLastSeenAt() throws Exception {
        assertThat(userRepository.findById(employee.getId()).get().getLastSeenAt()).isNull();

        Instant beforeCall = Instant.now().minus(1, ChronoUnit.SECONDS);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated());

        User refreshed = userRepository.findById(employee.getId()).orElseThrow();
        assertThat(refreshed.getLastSeenAt()).isNotNull();
        assertThat(refreshed.getLastSeenAt()).isAfterOrEqualTo(beforeCall);
    }

    @Test
    @DisplayName("POST /api/activities updates user lastSeenAt timestamp")
    void createActivity_UpdatesLastSeenAt() throws Exception {
        Instant oldLastSeen = Instant.now().minus(20, ChronoUnit.MINUTES);
        employee.setLastSeenAt(oldLastSeen);
        userRepository.save(employee);

        BrowserSession session = sessionRepository.save(BrowserSession.builder()
                .user(employee)
                .startedAt(Instant.now())
                .status(SessionStatus.ACTIVE)
                .build());

        CreateActivityRequest request = CreateActivityRequest.builder()
                .sessionId(session.getId())
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/new")
                .pageTitle("ChatGPT")
                .startedAt(Instant.now())
                .build();

        Instant beforeCall = Instant.now().minus(1, ChronoUnit.SECONDS);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        User refreshed = userRepository.findById(employee.getId()).orElseThrow();
        assertThat(refreshed.getLastSeenAt()).isNotNull();
        assertThat(refreshed.getLastSeenAt()).isAfterOrEqualTo(beforeCall);
        assertThat(refreshed.getLastSeenAt()).isAfter(oldLastSeen);
    }

    @Test
    @DisplayName("GET /api/auth/me does not trigger lastSeenAt update (avoids excessive writes)")
    void authMe_DoesNotUpdateLastSeenAt() throws Exception {
        assertThat(userRepository.findById(employee.getId()).get().getLastSeenAt()).isNull();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        User refreshed = userRepository.findById(employee.getId()).orElseThrow();
        assertThat(refreshed.getLastSeenAt()).isNull();
    }
}
