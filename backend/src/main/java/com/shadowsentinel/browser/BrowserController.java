package com.shadowsentinel.browser;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.dto.*;
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
@RequestMapping("/api")
public class BrowserController {

    private final BrowserService browserService;

    public BrowserController(BrowserService browserService) {
        this.browserService = browserService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionResponse> createSession(@AuthenticationPrincipal User currentUser) {
        CreateSessionResponse response = browserService.createSession(currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/sessions/{id}/end")
    public ResponseEntity<BrowserSessionResponse> endSession(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User currentUser) {
        BrowserSessionResponse response = browserService.endSession(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<BrowserSessionResponse>> getSessions(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BrowserSessionResponse> sessions = browserService.getSessions(currentUser, pageable);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/activities")
    public ResponseEntity<BrowserActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            @AuthenticationPrincipal User currentUser) {
        BrowserActivityResponse response = browserService.createActivity(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/activities")
    public ResponseEntity<Page<BrowserActivityResponse>> getActivities(
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BrowserActivityResponse> activities = browserService.getActivities(sessionId, domain, from, to, currentUser, pageable);
        return ResponseEntity.ok(activities);
    }
}
