package com.shadowsentinel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.alert.Alert;
import com.shadowsentinel.alert.AlertRepository;
import com.shadowsentinel.alert.AlertStatus;
import com.shadowsentinel.auth.dto.LoginRequest;
import com.shadowsentinel.auth.dto.RegisterRequest;
import com.shadowsentinel.browser.dto.CreateActivityRequest;
import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.classification.ClassificationResult;
import com.shadowsentinel.classification.ClassificationResultRepository;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.ml.MlClient;
import com.shadowsentinel.classification.ml.MlPrediction;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PostgresContainerLifecycleIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private ClassificationResultRepository classificationResultRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @MockBean
    private MlClient mlClient;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("PostgreSQL Container Integration Test: register -> login -> session -> activity -> evidence -> classification -> risk assessment -> CRITICAL alert")
    void postgresContainer_fullLifecycle_CriticalAlertGenerated() throws Exception {
        // 1. REGISTER
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("analyst.pg@sentinel.local")
                .password("Secur3P@ssw0rd")
                .companyId(20L)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("analyst.pg@sentinel.local"));

        // 2. LOGIN
        LoginRequest loginReq = LoginRequest.builder()
                .email("analyst.pg@sentinel.local")
                .password("Secur3P@ssw0rd")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();
        assertNotNull(token);
        String authHeader = "Bearer " + token;

        // 3. CREATE SESSION
        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", authHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode sessionJson = objectMapper.readTree(sessionResult.getResponse().getContentAsString());
        Long sessionId = sessionJson.get("id").asLong();

        // 4. POST ACTIVITY
        CreateActivityRequest activityReq = CreateActivityRequest.builder()
                .sessionId(sessionId)
                .domain("claude.ai")
                .url("https://claude.ai/chat/project-secret")
                .pageTitle("Confidential Data")
                .startedAt(Instant.now().minusSeconds(120))
                .endedAt(Instant.now())
                .build();

        MvcResult activityResult = mockMvc.perform(post("/api/activities")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activityReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode actJson = objectMapper.readTree(activityResult.getResponse().getContentAsString());
        Long activityId = actJson.get("id").asLong();

        // 5. MOCK ML CLIENT
        when(mlClient.predict(any())).thenReturn(MlPrediction.builder()
                .classLabel(ClassLabel.AI_GENERATION)
                .confidence(0.98)
                .modelVersion("v1")
                .build());

        // 6. POST EVIDENCE (Triggers critical risk: file upload + paste on AI domain)
        ClassificationEvidenceRequest evidenceReq = ClassificationEvidenceRequest.builder()
                .activityId(activityId)
                .schemaVersion("v1")
                .capturedAt(Instant.now())
                .domainLength(9)
                .visitCount(1)
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
                .fileUploadPresent(true)
                .promptSubmitCount(4)
                .generateClickCount(5)
                .pasteEventCount(3)
                .copyFromResponseCount(2)
                .typedCharCountBucket(4)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evidenceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(activityId.intValue()));

        // 7. VERIFY CLASSIFICATION RESULT PERSISTED
        Optional<ClassificationResult> classOpt = classificationResultRepository.findByActivityId(activityId);
        assertTrue(classOpt.isPresent(), "ClassificationResult must exist in database");
        ClassificationResult classResult = classOpt.get();
        assertEquals(ClassLabel.AI_GENERATION, classResult.getClassLabel());
        assertEquals(0.98, classResult.getConfidence(), 0.001);
        assertEquals("v1", classResult.getModelVersion());

        // 8. VERIFY RISK ASSESSMENT PERSISTED
        Optional<RiskAssessment> riskOpt = riskAssessmentRepository.findByActivityId(activityId);
        assertTrue(riskOpt.isPresent(), "RiskAssessment must exist in database");
        RiskAssessment riskAssessment = riskOpt.get();
        assertEquals(RiskLevel.CRITICAL, riskAssessment.getRiskLevel());
        assertEquals(100, riskAssessment.getRiskScore());
        assertTrue(riskAssessment.getMatchedRuleIds().contains("RULE_FILE_UPLOAD"));
        assertTrue(riskAssessment.getMatchedRuleIds().contains("RULE_CRITICAL_DATA_EXFIL"));

        // 9. VERIFY ALERT CREATED (CRITICAL)
        List<Alert> alerts = alertRepository.findAll();
        assertFalse(alerts.isEmpty(), "Alert must be created for CRITICAL risk");
        Alert alert = alerts.stream()
                .filter(a -> a.getActivity().getId().equals(activityId))
                .findFirst()
                .orElse(null);
        assertNotNull(alert, "Alert for activity must exist");
        assertEquals(activityId, alert.getActivity().getId());
        assertEquals(Severity.CRITICAL, alert.getSeverity());
        assertEquals(AlertStatus.NEW, alert.getStatus());
        assertTrue(alert.getTitle().contains("claude.ai"));
        assertTrue(alert.getTitle().contains("CRITICAL"));
        assertTrue(alert.getMessage().contains("CRITICAL"));
    }
}
