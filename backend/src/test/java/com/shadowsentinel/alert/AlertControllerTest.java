package com.shadowsentinel.alert;

import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.browser.*;
import com.shadowsentinel.classification.ClassificationEvidenceRepository;
import com.shadowsentinel.classification.ClassificationResultRepository;
import com.shadowsentinel.risk.RiskAssessment;
import com.shadowsentinel.risk.RiskAssessmentRepository;
import com.shadowsentinel.risk.RiskLevel;
import com.shadowsentinel.risk.Severity;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private ClassificationResultRepository resultRepository;

    @Autowired
    private ClassificationEvidenceRepository evidenceRepository;

    @Autowired
    private BrowserActivityRepository activityRepository;

    @Autowired
    private BrowserSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.shadowsentinel.TestDatabaseCleaner databaseCleaner;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private BrowserActivity activityA;
    private BrowserActivity activityA2;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        userA = userRepository.save(User.builder()
                .email("usera@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
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
                .url("https://chatgpt.com/c/1")
                .pageTitle("ChatGPT")
                .startedAt(Instant.now())
                .build());

        activityA2 = activityRepository.save(BrowserActivity.builder()
                .session(sessionA)
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/2")
                .pageTitle("ChatGPT Prompt 2")
                .startedAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("Alert created automatically for HIGH and CRITICAL risk; ignored for LOW and MEDIUM")
    void alertTrigger_SeverityLevels() {
        // LOW risk -> No alert created
        RiskAssessment lowAssessment = RiskAssessment.builder()
                .activity(activityA)
                .riskScore(10)
                .riskLevel(RiskLevel.LOW)
                .matchedRuleIds("NONE")
                .reasoning("Low risk")
                .policyVersion(1)
                .modelVersion("v1")
                .build();
        alertService.processRiskAssessment(lowAssessment);
        assertEquals(0, alertRepository.count(), "LOW risk should not trigger an alert");

        // MEDIUM risk -> No alert created
        RiskAssessment medAssessment = RiskAssessment.builder()
                .activity(activityA)
                .riskScore(35)
                .riskLevel(RiskLevel.MEDIUM)
                .matchedRuleIds("RULE_PASTE")
                .reasoning("Medium risk")
                .policyVersion(1)
                .modelVersion("v1")
                .build();
        alertService.processRiskAssessment(medAssessment);
        assertEquals(0, alertRepository.count(), "MEDIUM risk should not trigger an alert");

        // HIGH risk -> Creates alert
        RiskAssessment highAssessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(65)
                .riskLevel(RiskLevel.HIGH)
                .matchedRuleIds("RULE_FILE_UPLOAD")
                .reasoning("High risk file upload")
                .policyVersion(1)
                .modelVersion("v1")
                .build());
        Alert created = alertService.processRiskAssessment(highAssessment);
        assertNotNull(created);
        assertEquals(Severity.HIGH, created.getSeverity());
        assertEquals(AlertStatus.NEW, created.getStatus());
        assertEquals(1, created.getOccurrenceCount());
        assertEquals(1, alertRepository.count());
    }

    @Test
    @DisplayName("Deduplication: Same user + domain + severity within 30 min increments occurrenceCount")
    void alertDeduplication_IncrementsOccurrenceCount() {
        RiskAssessment assessment1 = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(85)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical data exfil 1")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert1 = alertService.processRiskAssessment(assessment1);
        assertEquals(1, alert1.getOccurrenceCount());
        assertEquals(1, alertRepository.count());

        // Second assessment on same user, domain, and CRITICAL severity within 30 min
        RiskAssessment assessment2 = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA2)
                .riskScore(90)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical data exfil 2")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert2 = alertService.processRiskAssessment(assessment2);
        assertEquals(alert1.getId(), alert2.getId(), "Must deduplicate to existing alert");
        assertEquals(2, alert2.getOccurrenceCount(), "Occurrence count should be 2");
        assertEquals(1, alertRepository.count(), "Total alert count should remain 1");
    }

    @Test
    @DisplayName("Deduplication does not apply to RESOLVED alerts; creates new alert instead")
    void deduplication_IgnoresResolvedAlerts() {
        RiskAssessment assessment1 = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(85)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical 1")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert1 = alertService.processRiskAssessment(assessment1);
        alertService.resolveAlert(alert1.getId(), userA);

        // Second assessment arrives after alert1 resolved
        RiskAssessment assessment2 = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA2)
                .riskScore(90)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical 2")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert2 = alertService.processRiskAssessment(assessment2);
        assertNotEquals(alert1.getId(), alert2.getId(), "Should create new alert since prior was resolved");
        assertEquals(2, alertRepository.count());
    }

    @Test
    @DisplayName("GET /api/alerts returns paged alerts with filters")
    void getAlerts_PagedWithFilters() throws Exception {
        RiskAssessment assessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(70)
                .riskLevel(RiskLevel.HIGH)
                .matchedRuleIds("RULE_FILE")
                .reasoning("File upload")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        alertService.processRiskAssessment(assessment);

        mockMvc.perform(get("/api/alerts")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("status", "NEW")
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].domain", is("chatgpt.com")))
                .andExpect(jsonPath("$.content[0].severity", is("HIGH")))
                .andExpect(jsonPath("$.content[0].status", is("NEW")));
    }

    @Test
    @DisplayName("PATCH /api/alerts/{id}/acknowledge and resolve update status")
    void alertStatusTransitions() throws Exception {
        RiskAssessment assessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(80)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical exfil")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert = alertService.processRiskAssessment(assessment);

        // Acknowledge
        mockMvc.perform(patch("/api/alerts/" + alert.getId() + "/acknowledge")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACKNOWLEDGED")))
                .andExpect(jsonPath("$.acknowledgedAt", notNullValue()));

        // Resolve
        mockMvc.perform(patch("/api/alerts/" + alert.getId() + "/resolve")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }

    @Test
    @DisplayName("Cross-user access returns 403 Forbidden for acknowledge and resolve")
    void crossUser_Forbidden() throws Exception {
        RiskAssessment assessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .activity(activityA)
                .riskScore(80)
                .riskLevel(RiskLevel.CRITICAL)
                .matchedRuleIds("RULE_CRITICAL")
                .reasoning("Critical exfil")
                .policyVersion(1)
                .modelVersion("v1")
                .build());

        Alert alert = alertService.processRiskAssessment(assessment);

        // User B attempts to acknowledge User A's alert
        mockMvc.perform(patch("/api/alerts/" + alert.getId() + "/acknowledge")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        // User B attempts to resolve User A's alert
        mockMvc.perform(patch("/api/alerts/" + alert.getId() + "/resolve")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/alerts with non-admin role returns 403 Forbidden")
    void getAlerts_NonAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/alerts")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Access denied: Admin role required")));
    }
}
