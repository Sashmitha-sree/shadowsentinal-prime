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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
@ActiveProfiles("test")
class FullLifecycleIntegrationTest {

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
    @DisplayName("End-to-End Flow: Register -> Login -> Session -> Activity -> Evidence -> ML Result -> Risk Assessment -> Alert (CRITICAL)")
    void fullLifecycle_CriticalAlertGenerated() throws Exception {
        // 1. REGISTER
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("analyst.e2e@sentinel.local")
                .password("Secur3P@ssw0rd")
                .companyId(10L)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("analyst.e2e@sentinel.local"));

        // 2. LOGIN
        LoginRequest loginReq = LoginRequest.builder()
                .email("analyst.e2e@sentinel.local")
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
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/classified-project")
                .pageTitle("Classified Synthesis")
                .startedAt(Instant.now().minusSeconds(180))
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
                .confidence(0.96)
                .modelVersion("v1")
                .build());

        // 6. POST EVIDENCE (Critical configuration: file upload + paste + high clicks on AI domain)
        ClassificationEvidenceRequest evidenceReq = ClassificationEvidenceRequest.builder()
                .activityId(activityId)
                .schemaVersion("v1")
                .capturedAt(Instant.now())
                .domainLength(11)
                .visitCount(1)
                .durationSeconds(180)
                .isKnownAiDomain(true)
                .hourOfDay(15)
                .pathDepth(2)
                .chatInterfacePresent(true)
                .promptInputPresent(true)
                .generateControlPresent(true)
                .regenerateControlPresent(true)
                .aiTermCount(4)
                .streamingOutputPresent(true)
                .fileUploadPresent(true)
                .promptSubmitCount(3)
                .generateClickCount(6)
                .pasteEventCount(2)
                .copyFromResponseCount(1)
                .typedCharCountBucket(3)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evidenceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(activityId.intValue()));

        // 7. VERIFY CLASSIFICATION RESULT PERSISTED
        Optional<ClassificationResult> classOpt = classificationResultRepository.findByActivityId(activityId);
        assertTrue(classOpt.isPresent(), "ClassificationResult must exist");
        ClassificationResult classResult = classOpt.get();
        assertEquals(ClassLabel.AI_GENERATION, classResult.getClassLabel());
        assertEquals(0.96, classResult.getConfidence(), 0.001);
        assertEquals("v1", classResult.getModelVersion());

        // 8. VERIFY RISK ASSESSMENT PERSISTED (Triggered by ClassificationCompletedEvent)
        Optional<RiskAssessment> riskOpt = riskAssessmentRepository.findByActivityId(activityId);
        assertTrue(riskOpt.isPresent(), "RiskAssessment must exist");
        RiskAssessment riskAssessment = riskOpt.get();
        assertEquals(RiskLevel.CRITICAL, riskAssessment.getRiskLevel(), "Must be CRITICAL risk level");
        assertEquals(100, riskAssessment.getRiskScore(), "Score clamped to 100");
        assertTrue(riskAssessment.getMatchedRuleIds().contains("RULE_FILE_UPLOAD"));
        assertTrue(riskAssessment.getMatchedRuleIds().contains("RULE_CRITICAL_DATA_EXFIL"));
        assertTrue(riskAssessment.getReasoning().contains("Final risk level: CRITICAL"));

        // 9. VERIFY ALERT CREATED (Triggered by RiskAssessedEvent for CRITICAL risk)
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
        assertEquals(1, alert.getOccurrenceCount());
        assertTrue(alert.getTitle().contains("chatgpt.com"));
        assertTrue(alert.getTitle().contains("CRITICAL"));
        assertTrue(alert.getMessage().contains("CRITICAL"));
    }
}
