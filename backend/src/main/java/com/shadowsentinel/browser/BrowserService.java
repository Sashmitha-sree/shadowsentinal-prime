package com.shadowsentinel.browser;

import com.shadowsentinel.auth.Role;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.dto.*;
import com.shadowsentinel.common.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BrowserService {

    private final BrowserSessionRepository sessionRepository;
    private final BrowserActivityRepository activityRepository;
    private final com.shadowsentinel.audit.AuditService auditService;

    public BrowserService(BrowserSessionRepository sessionRepository,
                          BrowserActivityRepository activityRepository,
                          com.shadowsentinel.audit.AuditService auditService) {
        this.sessionRepository = sessionRepository;
        this.activityRepository = activityRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CreateSessionResponse createSession(User currentUser) {
        Instant now = Instant.now();
        BrowserSession session = BrowserSession.builder()
                .user(currentUser)
                .startedAt(now)
                .status(SessionStatus.ACTIVE)
                .build();

        BrowserSession saved = sessionRepository.save(session);
        auditService.log(currentUser.getId(), com.shadowsentinel.audit.AuditEventType.SESSION_STARTED,
                saved.getId().toString(), "Browser session started");
        return new CreateSessionResponse(saved.getId());
    }

    @Transactional
    public BrowserSessionResponse endSession(Long sessionId, User currentUser) {
        BrowserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to access this session");
        }

        session.setEndedAt(Instant.now());
        session.setStatus(SessionStatus.CLOSED);

        BrowserSession updated = sessionRepository.save(session);
        return mapToSessionResponse(updated);
    }

    @Transactional(readOnly = true)
    public Page<BrowserSessionResponse> getSessions(User currentUser, Pageable pageable) {
        return sessionRepository.findByUserId(currentUser.getId(), pageable)
                .map(this::mapToSessionResponse);
    }

    @Transactional
    public BrowserActivityResponse createActivity(CreateActivityRequest request, User currentUser) {
        BrowserSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));

        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot add activity to a session belonging to another user");
        }

        Integer duration = request.getDurationSeconds();
        if (duration == null && request.getStartedAt() != null && request.getEndedAt() != null) {
            duration = (int) Math.max(0, Duration.between(request.getStartedAt(), request.getEndedAt()).getSeconds());
        }

        BrowserActivity activity = BrowserActivity.builder()
                .session(session)
                .domain(request.getDomain().trim().toLowerCase())
                .url(request.getUrl())
                .pageTitle(request.getPageTitle())
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .durationSeconds(duration)
                .build();

        BrowserActivity saved = activityRepository.save(activity);
        return mapToActivityResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<BrowserActivityResponse> getActivities(Long sessionId, String domain, Instant from, Instant to,
                                                      User currentUser, Pageable pageable) {
        Specification<BrowserActivity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User ownership predicate
            if (currentUser.getRole() != Role.ADMIN) {
                predicates.add(cb.equal(root.get("session").get("user").get("id"), currentUser.getId()));
            }

            if (sessionId != null) {
                predicates.add(cb.equal(root.get("session").get("id"), sessionId));
            }

            if (domain != null && !domain.isBlank()) {
                predicates.add(cb.equal(root.get("domain"), domain.trim().toLowerCase()));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startedAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return activityRepository.findAll(spec, pageable).map(this::mapToActivityResponse);
    }

    public BrowserSessionResponse mapToSessionResponse(BrowserSession session) {
        return BrowserSessionResponse.builder()
                .id(session.getId())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .build();
    }

    public BrowserActivityResponse mapToActivityResponse(BrowserActivity activity) {
        return BrowserActivityResponse.builder()
                .id(activity.getId())
                .sessionId(activity.getSession().getId())
                .domain(activity.getDomain())
                .url(activity.getUrl())
                .pageTitle(activity.getPageTitle())
                .startedAt(activity.getStartedAt())
                .endedAt(activity.getEndedAt())
                .durationSeconds(activity.getDurationSeconds())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
