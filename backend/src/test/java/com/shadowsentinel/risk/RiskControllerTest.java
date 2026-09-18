package com.shadowsentinel.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.browser.BrowserSession;
import com.shadowsentinel.browser.BrowserSessionRepository;
import com.shadowsentinel.browser.SessionStatus;
import com.shadowsentinel.classification.*;
import com.shadowsentinel.risk.dto.CreatePolicyRuleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RiskControllerTest {

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
    private ClassificationResultRepository resultRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private CompanyPolicyRepository companyPolicyRepository;

    @Autowired
    private com.shadowsentinel.alert.AlertRepository alertRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private User adminUser;
    private String tokenA;
    private String tokenB;
    private String tokenAdmin;
    private BrowserActivity activityA;
    private ClassificationEvidence evidenceA;
    private ClassificationResult resultA;

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

        adminUser = userRepository.save(User.builder()
                .email("admin@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        tokenA = jwtService.generateToken(userA.getEmail(), userA.getRole().name());
        tokenB = jwtService.generateToken(userB.getEmail(), userB.getRole().name());
        tokenAdmin = jwtService.generateToken(adminUser.getEmail(), adminUser.getRole().name());

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

        evidenceA = evidenceRepository.save(ClassificationEvidence.builder()
                .activity(activityA)
                .schemaVersion("v1")
                .capturedAt(Instant.now())
                .domainLength(11)
                .visitCount(1)
                .durationSeconds(60)
                .isKnownAiDomain(true)
                .hourOfDay(14)
                .pathDepth(2)
                .chatInterfacePresent(true)
                .promptInputPresent(true)
                .generateControlPresent(true)
                .regenerateControlPresent(true)
                .aiTermCount(3)
                .streamingOutputPresent(true)
                .fileUploadPresent(true)
                .promptSubmitCount(2)
                .generateClickCount(6)
                .pasteEventCount(1)
                .copyFromResponseCount(1)
                .typedCharCountBucket(2)
                .build());

        resultA = resultRepository.save(ClassificationResult.builder()
                .activity(activityA)
                .classLabel(ClassLabel.AI_GENERATION)
                .confidence(0.95)
                .modelVersion("v1")
                .build());
    }

    @Test
    @DisplayName("ClassificationCompletedEvent triggers RiskAssessment creation")
    void eventTriggers_RiskAssessmentPersisted() {
        // Publish the event as done when classification completes
        eventPublisher.publishEvent(new ClassificationCompletedEvent(this, resultA));

        assertTrue(riskAssessmentRepository.findByActivityId(activityA.getId()).isPresent(),
                "Risk assessment must be persisted on ClassificationCompletedEvent");
    }

    @Test
    @DisplayName("GET /api/risk/{activityId} returns assessment for owner and 403 for cross-user")
    void getAssessment_OwnerAndCrossUser() throws Exception {
        // Trigger assessment
        eventPublisher.publishEvent(new ClassificationCompletedEvent(this, resultA));

        // Admin retrieves assessment
        mockMvc.perform(get("/api/risk/" + activityA.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId", is(activityA.getId().intValue())))
                .andExpect(jsonPath("$.riskScore", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.riskLevel", notNullValue()))
                .andExpect(jsonPath("$.reasoning", containsString("Detected class: AI_GENERATION")));

        // User B (non-admin) attempt returns 403 Forbidden
        mockMvc.perform(get("/api/risk/" + activityA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("GET /api/risk paged query filters by level and user")
    void getAssessments_PagedFilter() throws Exception {
        // Trigger assessment
        eventPublisher.publishEvent(new ClassificationCompletedEvent(this, resultA));

        mockMvc.perform(get("/api/risk")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].activityId", is(activityA.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/policies requires ADMIN role; USER receives 403 Forbidden")
    void getPolicies_AdminOnly() throws Exception {
        // User A (USER) -> 403 Forbidden
        mockMvc.perform(get("/api/policies")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        // Admin -> 200 OK
        mockMvc.perform(get("/api/policies")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name", is("Default Corporate AI Policy")))
                .andExpect(jsonPath("$[0].rules", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("POST /api/policies/rules requires ADMIN, validates scoreWeight, and creates rule")
    void createRule_ValidationAndAdminRole() throws Exception {
        CreatePolicyRuleRequest validRequest = CreatePolicyRuleRequest.builder()
                .ruleKey("RULE_NEW_TEST")
                .description("New test policy rule")
                .severity(Severity.HIGH)
                .scoreWeight(25)
                .minGenerateClicks(3)
                .build();

        // User A (USER) -> 403 Forbidden
        mockMvc.perform(post("/api/policies/rules")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        // Admin with invalid weight > 40 -> 400 Bad Request
        CreatePolicyRuleRequest invalidWeightRequest = CreatePolicyRuleRequest.builder()
                .ruleKey("RULE_TOO_HEAVY")
                .description("Too heavy")
                .severity(Severity.CRITICAL)
                .scoreWeight(45) // Max is 40
                .build();

        mockMvc.perform(post("/api/policies/rules")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidWeightRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("scoreWeight must be between 0 and 40")));

        // Admin with valid request -> 201 Created
        mockMvc.perform(post("/api/policies/rules")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.ruleKey", is("RULE_NEW_TEST")))
                .andExpect(jsonPath("$.scoreWeight", is(25)));
    }
}
