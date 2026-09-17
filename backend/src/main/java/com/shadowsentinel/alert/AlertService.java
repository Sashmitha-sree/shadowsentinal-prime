package com.shadowsentinel.alert;

import com.shadowsentinel.alert.dto.AlertResponse;
import com.shadowsentinel.audit.AuditEventType;
import com.shadowsentinel.audit.AuditService;
import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.risk.RiskAssessment;
import com.shadowsentinel.risk.RiskLevel;
import com.shadowsentinel.risk.Severity;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final AuditService auditService;

    public AlertService(AlertRepository alertRepository, AuditService auditService) {
        this.alertRepository = alertRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Alert processRiskAssessment(RiskAssessment assessment) {
        if (assessment == null || assessment.getActivity() == null) {
            return null;
        }

        RiskLevel level = assessment.getRiskLevel();
        if (level != RiskLevel.HIGH && level != RiskLevel.CRITICAL) {
            return null;
        }

        Severity severity = level == RiskLevel.CRITICAL ? Severity.CRITICAL : Severity.HIGH;
        User user = assessment.getActivity().getSession().getUser();
        String domain = assessment.getActivity().getDomain();
        Instant cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);

        Optional<Alert> existingOpt = alertRepository
                .findFirstByUserIdAndActivityDomainAndSeverityAndStatusNotAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        user.getId(), domain, severity, AlertStatus.RESOLVED, cutoff);

        if (existingOpt.isPresent()) {
            Alert existing = existingOpt.get();
            existing.setOccurrenceCount(existing.getOccurrenceCount() + 1);
            Alert saved = alertRepository.save(existing);
            log.info("Deduplicated alert for user {} on domain {}: incremented occurrenceCount to {}",
                    user.getEmail(), domain, saved.getOccurrenceCount());

            auditService.log(user.getId(), AuditEventType.ALERT_CREATED, saved.getId().toString(),
                    "Alert occurrence incremented to " + saved.getOccurrenceCount() + " for domain " + domain);
            return saved;
        }

        String title = severity + " Risk Detected on " + domain;
        String message = assessment.getReasoning() != null ? assessment.getReasoning() : "Automated policy trigger";

        Alert alert = Alert.builder()
                .user(user)
                .activity(assessment.getActivity())
                .riskAssessment(assessment)
                .severity(severity)
                .title(title)
                .message(message)
                .status(AlertStatus.NEW)
                .occurrenceCount(1)
                .createdAt(Instant.now())
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Created new alert {} for user {} on domain {}: severity={}",
                saved.getId(), user.getEmail(), domain, severity);

        auditService.log(user.getId(), AuditEventType.ALERT_CREATED, saved.getId().toString(),
                "Alert created: " + saved.getTitle());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlerts(AlertStatus status, Severity severity, User currentUser, Pageable pageable) {
        Specification<Alert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getRole() != Role.ADMIN) {
                predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return alertRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long id, User currentUser) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        if (!alert.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Cannot acknowledge alert for another user");
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(Instant.now());
        Alert saved = alertRepository.save(alert);

        auditService.log(currentUser.getId(), AuditEventType.ALERT_ACKNOWLEDGED, saved.getId().toString(),
                "Alert acknowledged: " + saved.getTitle());

        return mapToResponse(saved);
    }

    @Transactional
    public AlertResponse resolveAlert(Long id, User currentUser) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        if (!alert.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Cannot resolve alert for another user");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        Alert saved = alertRepository.save(alert);

        return mapToResponse(saved);
    }

    public AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser().getId())
                .activityId(alert.getActivity().getId())
                .domain(alert.getActivity().getDomain())
                .riskAssessmentId(alert.getRiskAssessment().getId())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .occurrenceCount(alert.getOccurrenceCount())
                .createdAt(alert.getCreatedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .build();
    }
}
