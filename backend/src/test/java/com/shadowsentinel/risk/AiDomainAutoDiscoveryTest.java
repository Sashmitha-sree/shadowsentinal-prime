package com.shadowsentinel.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.TestDatabaseCleaner;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.browser.BrowserSession;
import com.shadowsentinel.browser.BrowserSessionRepository;
import com.shadowsentinel.browser.SessionStatus;
import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.ml.MlClient;
import com.shadowsentinel.classification.ml.MlPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiDomainAutoDiscoveryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiDomainRepository aiDomainRepository;

    @Autowired
    private BrowserSessionRepository sessionRepository;

    @Autowired
    private BrowserActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @MockBean
    private MlClient mlClient;

    private User employee;
    private String employeeToken;
    private BrowserSession session;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        employee = userRepository.save(User.builder()
                .email("analyst@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        employeeToken = jwtService.generateToken(employee.getEmail(), employee.getRole().name());

        session = sessionRepository.save(BrowserSession.builder()
                .user(employee)
                .startedAt(Instant.now())
                .status(SessionStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Auto-discovery inserts UNKNOWN on first AI-related evidence submission")
    void autoDiscovery_InsertsUnknownOnAiClassification() throws Exception {
        when(mlClient.predict(any())).thenReturn(MlPrediction.builder()
                .classLabel(ClassLabel.AI_GENERATION)
                .confidence(0.95)
                .modelVersion("v1")
                .build());

        BrowserActivity activity = activityRepository.save(BrowserActivity.builder()
                .session(session)
                .domain("claude.ai")
                .url("https://claude.ai/chat")
                .pageTitle("Claude")
                .startedAt(Instant.now())
                .build());

        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activity.getId())
                .schemaVersion("v1")
                .capturedAt(Instant.now())
                .domainLength(9)
                .isKnownAiDomain(true)
                .chatInterfacePresent(true)
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Optional<AiDomain> discoveredOpt = aiDomainRepository.findByDomain("claude.ai");
        assertTrue(discoveredOpt.isPresent(), "claude.ai should be auto-inserted into ai_domains");
        AiDomain discovered = discoveredOpt.get();
        assertEquals(AiDomainStatus.UNKNOWN, discovered.getStatus());
        assertNull(discovered.getAddedBy(), "addedBy must be null for auto-discovered domains");
        assertNotNull(discovered.getFirstSeenAt());
    }

    @Test
    @DisplayName("Auto-discovery does not duplicate domain on second AI evidence submission")
    void autoDiscovery_DoesNotDuplicateOnSubsequentEvidence() throws Exception {
        when(mlClient.predict(any())).thenReturn(MlPrediction.builder()
                .classLabel(ClassLabel.AI_INTERACTION)
                .confidence(0.90)
                .modelVersion("v1")
                .build());

        // First activity on chatgpt.com
        BrowserActivity activity1 = activityRepository.save(BrowserActivity.builder()
                .session(session)
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/1")
                .pageTitle("Chat 1")
                .startedAt(Instant.now())
                .build());

        ClassificationEvidenceRequest request1 = ClassificationEvidenceRequest.builder()
                .activityId(activity1.getId())
                .schemaVersion("v1")
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        assertEquals(1, aiDomainRepository.findAll().size());

        // Second activity on same domain chatgpt.com
        BrowserActivity activity2 = activityRepository.save(BrowserActivity.builder()
                .session(session)
                .domain("chatgpt.com")
                .url("https://chatgpt.com/c/2")
                .pageTitle("Chat 2")
                .startedAt(Instant.now())
                .build());

        ClassificationEvidenceRequest request2 = ClassificationEvidenceRequest.builder()
                .activityId(activity2.getId())
                .schemaVersion("v1")
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        List<AiDomain> allDomains = aiDomainRepository.findAll();
        assertEquals(1, allDomains.size(), "chatgpt.com must not be duplicated in ai_domains");
        assertEquals("chatgpt.com", allDomains.get(0).getDomain());
    }

    @Test
    @DisplayName("Auto-discovery does not insert domain when classified as NON_AI")
    void autoDiscovery_DoesNotInsertWhenNonAi() throws Exception {
        when(mlClient.predict(any())).thenReturn(MlPrediction.builder()
                .classLabel(ClassLabel.NON_AI)
                .confidence(0.99)
                .modelVersion("v1")
                .build());

        BrowserActivity activity = activityRepository.save(BrowserActivity.builder()
                .session(session)
                .domain("wikipedia.org")
                .url("https://wikipedia.org/wiki/Java")
                .pageTitle("Wikipedia")
                .startedAt(Instant.now())
                .build());

        ClassificationEvidenceRequest request = ClassificationEvidenceRequest.builder()
                .activityId(activity.getId())
                .schemaVersion("v1")
                .build();

        mockMvc.perform(post("/api/classification/evidence")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Optional<AiDomain> domainOpt = aiDomainRepository.findByDomain("wikipedia.org");
        assertTrue(domainOpt.isEmpty(), "NON_AI classified domain wikipedia.org should NOT be inserted into ai_domains");
        assertTrue(aiDomainRepository.findAll().isEmpty(), "No domains should be present");
    }
}
