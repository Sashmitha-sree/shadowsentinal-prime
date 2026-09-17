package com.shadowsentinel.browser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BrowserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrowserSessionRepository sessionRepository;

    @Autowired
    private BrowserActivityRepository activityRepository;

    @Autowired
    private com.shadowsentinel.alert.AlertRepository alertRepository;

    @Autowired
    private com.shadowsentinel.risk.RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private com.shadowsentinel.classification.ClassificationResultRepository resultRepository;

    @Autowired
    private com.shadowsentinel.classification.ClassificationEvidenceRepository evidenceRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        riskAssessmentRepository.deleteAll();
        resultRepository.deleteAll();
        evidenceRepository.deleteAll();
        activityRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(User.builder()
                .email("usera@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        userB = userRepository.save(User.builder()
                .email("userb@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        tokenA = jwtService.generateToken(userA.getEmail(), userA.getRole().name());
        tokenB = jwtService.generateToken(userB.getEmail(), userB.getRole().name());
    }

    @Test
    @DisplayName("POST /api/sessions creates ACTIVE session and returns id")
    void createSession_Success() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/sessions/{id}/end sets endedAt and status=CLOSED")
    void endSession_Success() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/sessions/" + sessionId + "/end")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sessionId.intValue())))
                .andExpect(jsonPath("$.status", is("CLOSED")))
                .andExpect(jsonPath("$.endedAt", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/sessions returns paged list of sessions for current user")
    void getSessions_Success() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/activities creates activity and calculates duration")
    void postActivity_Success() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        Instant start = Instant.now().minus(60, ChronoUnit.SECONDS);
        Instant end = Instant.now();

        CreateActivityRequest request = CreateActivityRequest.builder()
                .sessionId(sessionId)
                .domain("github.com")
                .url("https://github.com/org/repo")
                .pageTitle("GitHub Repository")
                .startedAt(start)
                .endedAt(end)
                .build();

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sessionId", is(sessionId.intValue())))
                .andExpect(jsonPath("$.domain", is("github.com")))
                .andExpect(jsonPath("$.url", is("https://github.com/org/repo")))
                .andExpect(jsonPath("$.durationSeconds", greaterThanOrEqualTo(59)));
    }

    @Test
    @DisplayName("GET /api/activities filters by domain, sessionId, and user")
    void getActivities_Filtered_Success() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        CreateActivityRequest act1 = CreateActivityRequest.builder()
                .sessionId(sessionId)
                .domain("github.com")
                .url("https://github.com")
                .startedAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .build();

        CreateActivityRequest act2 = CreateActivityRequest.builder()
                .sessionId(sessionId)
                .domain("stackoverflow.com")
                .url("https://stackoverflow.com")
                .startedAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .build();

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/activities?domain=github.com")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].domain", is("github.com")));
    }

    @Test
    @DisplayName("Cross-user access: User B ending User A's session returns 403 Forbidden")
    void crossUser_EndSession_Returns403() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionAId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/sessions/" + sessionAId + "/end")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("Cross-user access: User B posting activity to User A's session returns 403 Forbidden")
    void crossUser_PostActivity_Returns403() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionAId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        CreateActivityRequest request = CreateActivityRequest.builder()
                .sessionId(sessionAId)
                .domain("unauthorized-site.com")
                .startedAt(Instant.now())
                .build();

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Cannot add activity to a session belonging to another user")));
    }

    @Test
    @DisplayName("Unauthenticated request to /api/sessions returns 401 Unauthorized")
    void unauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(post("/api/sessions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
