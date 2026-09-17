package com.shadowsentinel.risk;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.classification.ClassificationEvidence;
import com.shadowsentinel.classification.ClassificationEvidenceRepository;
import com.shadowsentinel.classification.ClassificationResult;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.risk.dto.CompanyPolicyResponse;
import com.shadowsentinel.risk.dto.CreatePolicyRuleRequest;
import com.shadowsentinel.risk.dto.PolicyRuleResponse;
import com.shadowsentinel.risk.dto.RiskAssessmentResponse;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CompanyPolicyRepository companyPolicyRepository;
    private final PolicyRuleRepository policyRuleRepository;
    private final ClassificationEvidenceRepository evidenceRepository;
    private final BrowserActivityRepository activityRepository;
    private final RiskEngine riskEngine;

    public RiskAssessmentService(RiskAssessmentRepository riskAssessmentRepository,
                                 CompanyPolicyRepository companyPolicyRepository,
                                 PolicyRuleRepository policyRuleRepository,
                                 ClassificationEvidenceRepository evidenceRepository,
                                 BrowserActivityRepository activityRepository,
                                 RiskEngine riskEngine) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.companyPolicyRepository = companyPolicyRepository;
        this.policyRuleRepository = policyRuleRepository;
        this.evidenceRepository = evidenceRepository;
        this.activityRepository = activityRepository;
        this.riskEngine = riskEngine;
    }

    @Transactional
    public RiskAssessment assessClassification(ClassificationResult result) {
        if (result == null || result.getActivity() == null) {
            log.warn("Cannot evaluate risk assessment for null classification result or activity");
            return null;
        }

        Long activityId = result.getActivity().getId();
        Optional<RiskAssessment> existing = riskAssessmentRepository.findByActivityId(activityId);
        if (existing.isPresent()) {
            log.info("Risk assessment already exists for activity id: {}", activityId);
            return existing.get();
        }

        ClassificationEvidence evidence = evidenceRepository.findByActivityId(activityId)
                .orElse(null);

        if (evidence == null) {
            log.warn("No evidence found for activity id: {}. Assessment skipped.", activityId);
            return null;
        }

        CompanyPolicy policy = resolveActivePolicy(result.getActivity());
        RiskEngine.Evaluation evaluation = riskEngine.evaluate(evidence, result, policy);

        RiskAssessment assessment = RiskAssessment.builder()
                .activity(result.getActivity())
                .riskScore(evaluation.getRiskScore())
                .riskLevel(evaluation.getRiskLevel())
                .matchedRuleIds(evaluation.getMatchedRuleIds())
                .reasoning(evaluation.getReasoning())
                .policyVersion(evaluation.getPolicyVersion())
                .modelVersion(evaluation.getModelVersion())
                .build();

        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        log.info("Risk assessment saved for activity {}: score={}, level={}",
                activityId, saved.getRiskScore(), saved.getRiskLevel());

        return saved;
    }

    public CompanyPolicy resolveActivePolicy(BrowserActivity activity) {
        if (activity != null && activity.getSession() != null && activity.getSession().getUser() != null) {
            Long companyId = activity.getSession().getUser().getCompanyId();
            if (companyId != null) {
                Optional<CompanyPolicy> companyPolicy = companyPolicyRepository.findByCompanyIdAndActiveTrue(companyId);
                if (companyPolicy.isPresent()) {
                    return companyPolicy.get();
                }
            }
        }
        return companyPolicyRepository.findFirstByCompanyIdIsNullAndActiveTrueOrderByVersionDesc()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public RiskAssessmentResponse getAssessment(Long activityId, User currentUser) {
        BrowserActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        if (!activity.getSession().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot access risk assessment for another user's activity");
        }

        RiskAssessment assessment = riskAssessmentRepository.findByActivityId(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("No risk assessment found for activity id: " + activityId));

        return mapToAssessmentResponse(assessment);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentResponse> getAssessments(RiskLevel level, Instant from, Instant to,
                                                       User currentUser, Pageable pageable) {
        Specification<RiskAssessment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User isolation
            predicates.add(cb.equal(root.get("activity").get("session").get("user").get("id"), currentUser.getId()));

            if (level != null) {
                predicates.add(cb.equal(root.get("riskLevel"), level));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return riskAssessmentRepository.findAll(spec, pageable).map(this::mapToAssessmentResponse);
    }

    @Transactional(readOnly = true)
    public List<CompanyPolicyResponse> getPolicies() {
        return companyPolicyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyRuleResponse addRuleToPolicy(CreatePolicyRuleRequest request) {
        CompanyPolicy policy;
        if (request.getPolicyId() != null) {
            policy = companyPolicyRepository.findById(request.getPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + request.getPolicyId()));
        } else {
            policy = companyPolicyRepository.findFirstByCompanyIdIsNullAndActiveTrueOrderByVersionDesc()
                    .orElseGet(() -> companyPolicyRepository.save(CompanyPolicy.builder()
                            .name("Default Corporate AI Policy")
                            .active(true)
                            .version(1)
                            .build()));
        }

        PolicyRule rule = PolicyRule.builder()
                .policy(policy)
                .ruleKey(request.getRuleKey())
                .appliesToLabel(request.getAppliesToLabel())
                .domainPattern(request.getDomainPattern())
                .minGenerateClicks(request.getMinGenerateClicks())
                .requiresFileUpload(request.getRequiresFileUpload())
                .requiresPasteEvent(request.getRequiresPasteEvent())
                .severity(request.getSeverity())
                .scoreWeight(request.getScoreWeight())
                .description(request.getDescription())
                .build();

        PolicyRule savedRule = policyRuleRepository.save(rule);
        policy.addRule(savedRule);
        policy.setVersion(policy.getVersion() + 1);
        companyPolicyRepository.save(policy);

        return mapToRuleResponse(savedRule);
    }

    public RiskAssessmentResponse mapToAssessmentResponse(RiskAssessment assessment) {
        return RiskAssessmentResponse.builder()
                .id(assessment.getId())
                .activityId(assessment.getActivity().getId())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .matchedRuleIds(assessment.getMatchedRuleIds())
                .reasoning(assessment.getReasoning())
                .policyVersion(assessment.getPolicyVersion())
                .modelVersion(assessment.getModelVersion())
                .createdAt(assessment.getCreatedAt())
                .build();
    }

    public CompanyPolicyResponse mapToPolicyResponse(CompanyPolicy policy) {
        List<PolicyRuleResponse> ruleResponses = policy.getRules() != null ?
                policy.getRules().stream().map(this::mapToRuleResponse).collect(Collectors.toList()) :
                List.of();

        return CompanyPolicyResponse.builder()
                .id(policy.getId())
                .companyId(policy.getCompanyId())
                .name(policy.getName())
                .active(policy.isActive())
                .version(policy.getVersion())
                .createdAt(policy.getCreatedAt())
                .rules(ruleResponses)
                .build();
    }

    public PolicyRuleResponse mapToRuleResponse(PolicyRule rule) {
        return PolicyRuleResponse.builder()
                .id(rule.getId())
                .policyId(rule.getPolicy() != null ? rule.getPolicy().getId() : null)
                .ruleKey(rule.getRuleKey())
                .appliesToLabel(rule.getAppliesToLabel())
                .domainPattern(rule.getDomainPattern())
                .minGenerateClicks(rule.getMinGenerateClicks())
                .requiresFileUpload(rule.getRequiresFileUpload())
                .requiresPasteEvent(rule.getRequiresPasteEvent())
                .severity(rule.getSeverity())
                .scoreWeight(rule.getScoreWeight())
                .description(rule.getDescription())
                .build();
    }
}
