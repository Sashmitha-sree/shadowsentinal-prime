package com.shadowsentinel.classification;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.dto.ClassificationEvidenceResponse;
import com.shadowsentinel.classification.dto.ClassificationResultResponse;
import com.shadowsentinel.classification.ml.MlClient;
import com.shadowsentinel.classification.ml.MlPrediction;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.common.UserAlreadyExistsException;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    private final ClassificationEvidenceRepository evidenceRepository;
    private final ClassificationResultRepository resultRepository;
    private final BrowserActivityRepository activityRepository;
    private final MlClient mlClient;
    private final ApplicationEventPublisher eventPublisher;

    public ClassificationService(ClassificationEvidenceRepository evidenceRepository,
                                 ClassificationResultRepository resultRepository,
                                 BrowserActivityRepository activityRepository,
                                 MlClient mlClient,
                                 ApplicationEventPublisher eventPublisher) {
        this.evidenceRepository = evidenceRepository;
        this.resultRepository = resultRepository;
        this.activityRepository = activityRepository;
        this.mlClient = mlClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ClassificationEvidenceResponse submitEvidence(ClassificationEvidenceRequest request, User currentUser) {
        BrowserActivity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + request.getActivityId()));

        if (!activity.getSession().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot submit classification evidence for another user's activity");
        }

        if (evidenceRepository.existsByActivityId(request.getActivityId())) {
            throw new UserAlreadyExistsException("Classification evidence already exists for activity id: " + request.getActivityId());
        }

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .schemaVersion(request.getSchemaVersion() != null ? request.getSchemaVersion() : "v1")
                .capturedAt(request.getCapturedAt() != null ? request.getCapturedAt() : Instant.now())
                .domainLength(request.getDomainLength())
                .visitCount(request.getVisitCount())
                .durationSeconds(request.getDurationSeconds())
                .isKnownAiDomain(request.isKnownAiDomain())
                .hourOfDay(request.getHourOfDay())
                .pathDepth(request.getPathDepth())
                .chatInterfacePresent(request.isChatInterfacePresent())
                .promptInputPresent(request.isPromptInputPresent())
                .generateControlPresent(request.isGenerateControlPresent())
                .regenerateControlPresent(request.isRegenerateControlPresent())
                .aiTermCount(request.getAiTermCount())
                .streamingOutputPresent(request.isStreamingOutputPresent())
                .fileUploadPresent(request.isFileUploadPresent())
                .promptSubmitCount(request.getPromptSubmitCount())
                .generateClickCount(request.getGenerateClickCount())
                .pasteEventCount(request.getPasteEventCount())
                .copyFromResponseCount(request.getCopyFromResponseCount())
                .typedCharCountBucket(request.getTypedCharCountBucket())
                .build();

        ClassificationEvidence saved = evidenceRepository.save(evidence);

        // ML inference: non-blocking resilience
        try {
            MlPrediction prediction = mlClient.predict(saved);
            if (prediction != null && prediction.getClassLabel() != null) {
                ClassificationResult result = ClassificationResult.builder()
                        .activity(activity)
                        .classLabel(prediction.getClassLabel())
                        .confidence(prediction.getConfidence())
                        .modelVersion(prediction.getModelVersion() != null ? prediction.getModelVersion() : "v1")
                        .build();

                ClassificationResult savedResult = resultRepository.save(result);
                eventPublisher.publishEvent(new ClassificationCompletedEvent(this, savedResult));
                log.info("Classification completed for activity {}: {} (confidence: {})",
                        activity.getId(), savedResult.getClassLabel(), savedResult.getConfidence());
            }
        } catch (Exception ex) {
            log.warn("ML inference service unreachable or failed for activity {}: {}. Evidence preserved.",
                    activity.getId(), ex.getMessage());
        }

        return mapToEvidenceResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClassificationEvidenceResponse getEvidence(Long activityId, User currentUser) {
        BrowserActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        if (!activity.getSession().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot access classification evidence for another user's activity");
        }

        ClassificationEvidence evidence = evidenceRepository.findByActivityId(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("No classification evidence found for activity id: " + activityId));

        return mapToEvidenceResponse(evidence);
    }

    @Transactional(readOnly = true)
    public ClassificationResultResponse getResult(Long activityId, User currentUser) {
        BrowserActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        if (!activity.getSession().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot access classification result for another user's activity");
        }

        ClassificationResult result = resultRepository.findByActivityId(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("No classification result found for activity id: " + activityId));

        return mapToResultResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<ClassificationResultResponse> getResults(ClassLabel label, Instant from, Instant to,
                                                         User currentUser, Pageable pageable) {
        Specification<ClassificationResult> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User isolation check
            predicates.add(cb.equal(root.get("activity").get("session").get("user").get("id"), currentUser.getId()));

            if (label != null) {
                predicates.add(cb.equal(root.get("classLabel"), label));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return resultRepository.findAll(spec, pageable).map(this::mapToResultResponse);
    }

    public ClassificationEvidenceResponse mapToEvidenceResponse(ClassificationEvidence evidence) {
        return ClassificationEvidenceResponse.builder()
                .id(evidence.getId())
                .activityId(evidence.getActivity().getId())
                .schemaVersion(evidence.getSchemaVersion())
                .capturedAt(evidence.getCapturedAt())
                .createdAt(evidence.getCreatedAt())
                .domainLength(evidence.getDomainLength())
                .visitCount(evidence.getVisitCount())
                .durationSeconds(evidence.getDurationSeconds())
                .isKnownAiDomain(evidence.isKnownAiDomain())
                .hourOfDay(evidence.getHourOfDay())
                .pathDepth(evidence.getPathDepth())
                .chatInterfacePresent(evidence.isChatInterfacePresent())
                .promptInputPresent(evidence.isPromptInputPresent())
                .generateControlPresent(evidence.isGenerateControlPresent())
                .regenerateControlPresent(evidence.isRegenerateControlPresent())
                .aiTermCount(evidence.getAiTermCount())
                .streamingOutputPresent(evidence.isStreamingOutputPresent())
                .fileUploadPresent(evidence.isFileUploadPresent())
                .promptSubmitCount(evidence.getPromptSubmitCount())
                .generateClickCount(evidence.getGenerateClickCount())
                .pasteEventCount(evidence.getPasteEventCount())
                .copyFromResponseCount(evidence.getCopyFromResponseCount())
                .typedCharCountBucket(evidence.getTypedCharCountBucket())
                .build();
    }

    public ClassificationResultResponse mapToResultResponse(ClassificationResult result) {
        return ClassificationResultResponse.builder()
                .id(result.getId())
                .activityId(result.getActivity().getId())
                .classLabel(result.getClassLabel())
                .confidence(result.getConfidence())
                .modelVersion(result.getModelVersion())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
