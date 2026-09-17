package com.shadowsentinel.classification;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.dto.ClassificationEvidenceResponse;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.common.UserAlreadyExistsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ClassificationService {

    private final ClassificationEvidenceRepository evidenceRepository;
    private final BrowserActivityRepository activityRepository;

    public ClassificationService(ClassificationEvidenceRepository evidenceRepository,
                                 BrowserActivityRepository activityRepository) {
        this.evidenceRepository = evidenceRepository;
        this.activityRepository = activityRepository;
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
        return mapToResponse(saved);
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

        return mapToResponse(evidence);
    }

    public ClassificationEvidenceResponse mapToResponse(ClassificationEvidence evidence) {
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
}
