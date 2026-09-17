package com.shadowsentinel.risk;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.risk.dto.RiskAssessmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<RiskAssessmentResponse> getAssessment(
            @PathVariable Long activityId,
            @AuthenticationPrincipal User currentUser) {
        RiskAssessmentResponse response = riskAssessmentService.getAssessment(activityId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RiskAssessmentResponse>> getAssessments(
            @RequestParam(required = false) RiskLevel level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Page<RiskAssessmentResponse> response = riskAssessmentService.getAssessments(level, from, to, currentUser, pageable);
        return ResponseEntity.ok(response);
    }
}
