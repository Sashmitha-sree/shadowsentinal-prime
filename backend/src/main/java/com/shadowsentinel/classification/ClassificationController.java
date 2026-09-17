package com.shadowsentinel.classification;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.classification.dto.ClassificationEvidenceRequest;
import com.shadowsentinel.classification.dto.ClassificationEvidenceResponse;
import com.shadowsentinel.classification.dto.ClassificationResultResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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

    @GetMapping("/results/{activityId}")
    public ResponseEntity<ClassificationResultResponse> getResult(
            @PathVariable("activityId") Long activityId,
            @AuthenticationPrincipal User currentUser) {
        ClassificationResultResponse response = classificationService.getResult(activityId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results")
    public ResponseEntity<Page<ClassificationResultResponse>> getResults(
            @RequestParam(value = "label", required = false) ClassLabel label,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ClassificationResultResponse> results = classificationService.getResults(label, from, to, currentUser, pageable);
        return ResponseEntity.ok(results);
    }
}
