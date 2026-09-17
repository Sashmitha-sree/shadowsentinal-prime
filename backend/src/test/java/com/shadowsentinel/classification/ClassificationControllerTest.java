package com.shadowsentinel.classification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.browser.*;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassificationControllerTest {

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
    private ClassificationEvidenceRepository evidenceRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private BrowserActivity activityA;

    @BeforeEach
    void setUp() {
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

        BrowserSession sessionA = sessionRepository.save(BrowserSession.builder()
                .user(userA)
                .startedAt(Instant.now())
                .status(SessionStatus.ACTIVE)
                .build());

        activityA = activityRepository.save(BrowserActivity.builder()
                .session(sessionA)
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/123")
                .pageTitle("ChatGPT")
                .startedAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("POST /api/classification/evidence valid submission returns 201 Created with 18 fields")
    void submitEvidence_ValidSubmit() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .schemaVersion("v1")
                .capturedAt(Instant.now())
                .domainLength(11)
                .visitCount(3)
                .durationSeconds(120)
                .isKnownAiDomain(true)
                .hourOfDay(14)
                .pathDepth(2)
                .chatInterfacePresent(true)
                .promptInputPresent(true)
                .generateControlPresent(true)
                .regenerateControlPresent(true)
                .aiTermCount(5)
                .streamingOutputPresent(true)
                .fileUploadPresent(false)
                .promptSubmitCount(2)
                .generateClickCount(2)
                .pasteEventCount(1)
                .copyFromResponseCount(1)
                .typedCharCountBucket(2)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.activityId", is(activityA.getId().intValue())))
                .andExpect(jsonPath("$.schemaVersion", is("v1")))
                .andExpect(jsonPath("$.domainLength", is(11)))
                .andExpect(jsonPath("$.isKnownAiDomain", is(true)))
                .andExpect(jsonPath("$.hourOfDay", is(14)))
                .andExpect(jsonPath("$.chatInterfacePresent", is(true)))
                .andExpect(jsonPath("$.typedCharCountBucket", is(2)));
    }

    @Test
    @DisplayName("POST /api/classification/evidence rejects out-of-range hourOfDay with 400 Bad Request")
    void submitEvidence_OutOfRangeHourOfDay_Returns400() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .hourOfDay(24) // Out of range: max is 23
                .typedCharCountBucket(2)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("hourOfDay must be between 0 and 23")));
    }

    @Test
    @DisplayName("POST /api/classification/evidence rejects out-of-range typedCharCountBucket with 400 Bad Request")
    void submitEvidence_OutOfRangeBucket_Returns400() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .hourOfDay(12)
                .typedCharCountBucket(5) // Out of range: max is 4
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("typedCharCountBucket must be between 0 and 4")));
    }

    @Test
    @DisplayName("POST /api/classification/evidence duplicate submission returns 409 Conflict")
    void submitEvidence_DuplicateReturns409() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .hourOfDay(10)
                .typedCharCountBucket(1)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate submission for same activityId
        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already exists for activity id")));
    }

    @Test
    @DisplayName("GET /api/classification/evidence/{activityId} returns existing evidence")
    void getEvidence_Success() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .domainLength(15)
                .hourOfDay(9)
                .chatInterfacePresent(true)
                .typedCharCountBucket(3)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/classification/evidence/" + activityA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId", is(activityA.getId().intValue())))
                .andExpect(jsonPath("$.domainLength", is(15)))
                .andExpect(jsonPath("$.chatInterfacePresent", is(true)))
                .andExpect(jsonPath("$.typedCharCountBucket", is(3)));
    }

    @Test
    @DisplayName("Cross-user access returns 403 Forbidden")
    void crossUser_Access_Returns403() throws Exception {
        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activityA.getId())
                .hourOfDay(16)
                .typedCharCountBucket(1)
                .build();

        // User B attempts to submit evidence for User A's activity
        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Cannot submit classification evidence for another user's activity")));
    }
}
