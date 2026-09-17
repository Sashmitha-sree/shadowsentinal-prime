package com.shadowsentinel.classification;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.dto.ClassificationEvidenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping("/evidence")
    public ResponseEntity<ClassificationEvidenceResponse> submitEvidence(
            @Valid @RequestBody ClassificationEvidenceRequest request,
            @AuthenticationPrincipal User currentUser) {
        ClassificationEvidenceResponse response = classificationService.submitEvidence(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/evidence/{activityId}")
    public ResponseEntity<ClassificationEvidenceResponse> getEvidence(
            @PathVariable("activityId") Long activityId,
            @AuthenticationPrincipal User currentUser) {
        ClassificationEvidenceResponse response = classificationService.getEvidence(activityId, currentUser);
        return ResponseEntity.ok(response);
    }
}
