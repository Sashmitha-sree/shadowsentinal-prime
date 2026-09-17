package com.shadowsentinel.audit;

import com.shadowsentinel.audit.dto.AuditLogResponse;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Single audit log entry point called across services.
     * Guaranteed fail-safe: wraps write in try/catch and never breaks the main flow.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, AuditEventType eventType, String targetId, String details) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .targetId(targetId)
                    .details(details)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Audit logging failed for event {}: {}. Main execution flow preserved.",
                    eventType, ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(AuditEventType eventType, Long userId,
                                               Instant from, Instant to, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .eventType(log.getEventType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
