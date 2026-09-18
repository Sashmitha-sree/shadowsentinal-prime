package com.shadowsentinel.risk;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.risk.dto.AiDomainResponse;
import com.shadowsentinel.risk.dto.CreateAiDomainRequest;
import com.shadowsentinel.risk.dto.UpdateAiDomainRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-domains")
public class AiDomainController {

    private final AiDomainService aiDomainService;

    public AiDomainController(AiDomainService aiDomainService) {
        this.aiDomainService = aiDomainService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AiDomainResponse>> getAiDomains(
            @RequestParam(required = false) AiDomainStatus status,
            @PageableDefault(size = 20, sort = "firstSeenAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AiDomainResponse> response = aiDomainService.getAiDomains(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiDomainResponse> createAiDomain(
            @Valid @RequestBody CreateAiDomainRequest request,
            @AuthenticationPrincipal User currentUser) {
        AiDomainResponse response = aiDomainService.createAiDomain(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiDomainResponse> updateAiDomain(
            @PathVariable Long id,
            @RequestBody UpdateAiDomainRequest request,
            @AuthenticationPrincipal User currentUser) {
        AiDomainResponse response = aiDomainService.updateAiDomain(id, request, currentUser);
        return ResponseEntity.ok(response);
    }
}
