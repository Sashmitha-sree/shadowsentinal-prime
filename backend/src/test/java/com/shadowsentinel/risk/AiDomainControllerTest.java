package com.shadowsentinel.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowsentinel.TestDatabaseCleaner;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.auth.security.JwtService;
import com.shadowsentinel.risk.dto.CreateAiDomainRequest;
import com.shadowsentinel.risk.dto.UpdateAiDomainRequest;
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
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiDomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiDomainRepository aiDomainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private User admin;
    private User employee;
    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        admin = userRepository.save(User.builder()
                .email("admin@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        employee = userRepository.save(User.builder()
                .email("worker@sentinel.local")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        adminToken = jwtService.generateToken(admin.getEmail(), admin.getRole().name());
        employeeToken = jwtService.generateToken(employee.getEmail(), employee.getRole().name());
    }

    @Test
    @DisplayName("GET /api/ai-domains returns paged list of all AI domains")
    void getAiDomains_Unfiltered_ReturnsPagedList() throws Exception {
        aiDomainRepository.save(AiDomain.builder()
                .domain("chatgpt.com")
                .status(AiDomainStatus.APPROVED)
                .addedBy("admin@sentinel.local")
                .firstSeenAt(Instant.now())
                .build());

        aiDomainRepository.save(AiDomain.builder()
                .domain("shadowai.io")
                .status(AiDomainStatus.BLOCKED)
                .addedBy("admin@sentinel.local")
                .firstSeenAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/ai-domains")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /api/ai-domains?status=BLOCKED filters list by status")
    void getAiDomains_FilteredByStatus_ReturnsMatchingOnly() throws Exception {
        aiDomainRepository.save(AiDomain.builder()
                .domain("approved.ai")
                .status(AiDomainStatus.APPROVED)
                .firstSeenAt(Instant.now())
                .build());

        aiDomainRepository.save(AiDomain.builder()
                .domain("blocked.ai")
                .status(AiDomainStatus.BLOCKED)
                .firstSeenAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/ai-domains")
                        .param("status", "BLOCKED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].domain", is("blocked.ai")))
                .andExpect(jsonPath("$.content[0].status", is("BLOCKED")));
    }

    @Test
    @DisplayName("POST /api/ai-domains manually adds domain and sets addedBy from authenticated admin")
    void createAiDomain_Success() throws Exception {
        CreateAiDomainRequest request = CreateAiDomainRequest.builder()
                .domain("deepseek.com")
                .status(AiDomainStatus.BLOCKED)
                .notes("High risk of unvetted code telemetry")
                .build();

        mockMvc.perform(post("/api/ai-domains")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.domain", is("deepseek.com")))
                .andExpect(jsonPath("$.status", is("BLOCKED")))
                .andExpect(jsonPath("$.addedBy", is("admin@sentinel.local")))
                .andExpect(jsonPath("$.notes", is("High risk of unvetted code telemetry")))
                .andExpect(jsonPath("$.firstSeenAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        Optional<AiDomain> persisted = aiDomainRepository.findByDomain("deepseek.com");
        assertTrue(persisted.isPresent());
        assertEquals("admin@sentinel.local", persisted.get().getAddedBy());
        assertEquals(AiDomainStatus.BLOCKED, persisted.get().getStatus());
    }

    @Test
    @DisplayName("POST /api/ai-domains duplicate domain returns 409 Conflict")
    void createAiDomain_Duplicate_Returns409() throws Exception {
        aiDomainRepository.save(AiDomain.builder()
                .domain("chatgpt.com")
                .status(AiDomainStatus.APPROVED)
                .firstSeenAt(Instant.now())
                .build());

        CreateAiDomainRequest duplicate = CreateAiDomainRequest.builder()
                .domain("chatgpt.com")
                .status(AiDomainStatus.BLOCKED)
                .build();

        mockMvc.perform(post("/api/ai-domains")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("PATCH /api/ai-domains/{id} updates status, notes, and sets addedBy from authenticated admin")
    void updateAiDomain_Success() throws Exception {
        AiDomain domain = aiDomainRepository.save(AiDomain.builder()
                .domain("claude.ai")
                .status(AiDomainStatus.UNKNOWN)
                .firstSeenAt(Instant.now().minusSeconds(3600))
                .build());

        UpdateAiDomainRequest patch = UpdateAiDomainRequest.builder()
                .status(AiDomainStatus.APPROVED)
                .notes("Approved for developer research tier")
                .build();

        mockMvc.perform(patch("/api/ai-domains/" + domain.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(domain.getId().intValue())))
                .andExpect(jsonPath("$.domain", is("claude.ai")))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.addedBy", is("admin@sentinel.local")))
                .andExpect(jsonPath("$.notes", is("Approved for developer research tier")))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        AiDomain updated = aiDomainRepository.findById(domain.getId()).orElseThrow();
        assertEquals(AiDomainStatus.APPROVED, updated.getStatus());
        assertEquals("admin@sentinel.local", updated.getAddedBy());
        assertEquals("Approved for developer research tier", updated.getNotes());
    }

    @Test
    @DisplayName("PATCH /api/ai-domains/{id} non-existent returns 404 Not Found")
    void updateAiDomain_NotFound() throws Exception {
        UpdateAiDomainRequest patch = UpdateAiDomainRequest.builder()
                .status(AiDomainStatus.APPROVED)
                .build();

        mockMvc.perform(patch("/api/ai-domains/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Non-admin (USER) accessing /api/ai-domains returns 403 Forbidden")
    void nonAdmin_Access_Returns403() throws Exception {
        mockMvc.perform(get("/api/ai-domains")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Admin role required")));

        mockMvc.perform(post("/api/ai-domains")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAiDomainRequest("test.ai", AiDomainStatus.UNKNOWN, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated access returns 401 Unauthorized")
    void unauthenticated_Access_Returns401() throws Exception {
        mockMvc.perform(get("/api/ai-domains"))
                .andExpect(status().isUnauthorized());
    }
}
