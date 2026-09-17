package com.shadowsentinel.risk;

import com.shadowsentinel.risk.dto.CompanyPolicyResponse;
import com.shadowsentinel.risk.dto.CreatePolicyRuleRequest;
import com.shadowsentinel.risk.dto.PolicyRuleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final RiskAssessmentService riskAssessmentService;

    public PolicyController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompanyPolicyResponse>> getPolicies() {
        List<CompanyPolicyResponse> policies = riskAssessmentService.getPolicies();
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyRuleResponse> addRule(@Valid @RequestBody CreatePolicyRuleRequest request) {
        PolicyRuleResponse response = riskAssessmentService.addRuleToPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
