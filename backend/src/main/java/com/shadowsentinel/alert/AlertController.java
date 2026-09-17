package com.shadowsentinel.alert;

import com.shadowsentinel.alert.dto.AlertResponse;
import com.shadowsentinel.auth.User;
import com.shadowsentinel.risk.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Page<AlertResponse> response = alertService.getAlerts(status, severity, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        AlertResponse response = alertService.acknowledgeAlert(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        AlertResponse response = alertService.resolveAlert(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
