package com.shadowsentinel.risk;

import com.shadowsentinel.classification.ClassLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RiskPolicyDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RiskPolicyDataLoader.class);

    private final CompanyPolicyRepository companyPolicyRepository;

    public RiskPolicyDataLoader(CompanyPolicyRepository companyPolicyRepository) {
        this.companyPolicyRepository = companyPolicyRepository;
    }

    @Override
    public void run(String... args) {
        if (!companyPolicyRepository.existsByActiveTrue()) {
            log.info("No active risk policy found. Seeding default corporate AI policy with 5 baseline rules...");

            CompanyPolicy defaultPolicy = CompanyPolicy.builder()
                    .name("Default Corporate AI Policy")
                    .active(true)
                    .version(1)
                    .createdAt(Instant.now())
                    .build();

            // 1. File upload to an AI service
            PolicyRule rule1 = PolicyRule.builder()
                    .ruleKey("RULE_FILE_UPLOAD")
                    .description("File uploaded to an AI service")
                    .severity(Severity.HIGH)
                    .scoreWeight(30)
                    .requiresFileUpload(true)
                    .build();

            // 2. Pasting into an AI prompt
            PolicyRule rule2 = PolicyRule.builder()
                    .ruleKey("RULE_PASTE_PROMPT")
                    .description("Pasting content into an AI prompt")
                    .severity(Severity.MEDIUM)
                    .scoreWeight(15)
                    .requiresPasteEvent(true)
                    .build();

            // 3. High generation volume
            PolicyRule rule3 = PolicyRule.builder()
                    .ruleKey("RULE_HIGH_GEN_VOLUME")
                    .description("High volume of generation requests")
                    .severity(Severity.HIGH)
                    .scoreWeight(25)
                    .minGenerateClicks(5)
                    .build();

            // 4. Unapproved external AI domain
            PolicyRule rule4 = PolicyRule.builder()
                    .ruleKey("RULE_UNAPPROVED_AI_DOMAIN")
                    .description("Interaction on an unapproved external AI service")
                    .severity(Severity.MEDIUM)
                    .scoreWeight(20)
                    .domainPattern("*chatgpt.com*")
                    .build();

            // 5. Critical data exfiltration (file upload + AI generation)
            PolicyRule rule5 = PolicyRule.builder()
                    .ruleKey("RULE_CRITICAL_DATA_EXFIL")
                    .description("File upload combined with AI Generation")
                    .severity(Severity.CRITICAL)
                    .scoreWeight(35)
                    .appliesToLabel(ClassLabel.AI_GENERATION)
                    .requiresFileUpload(true)
                    .build();

            defaultPolicy.addRule(rule1);
            defaultPolicy.addRule(rule2);
            defaultPolicy.addRule(rule3);
            defaultPolicy.addRule(rule4);
            defaultPolicy.addRule(rule5);

            companyPolicyRepository.save(defaultPolicy);
            log.info("Default corporate AI policy seeded successfully with 5 rules.");
        }
    }
}
