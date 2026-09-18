package com.shadowsentinel.risk;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.common.DuplicateResourceException;
import com.shadowsentinel.common.ResourceNotFoundException;
import com.shadowsentinel.risk.dto.AiDomainResponse;
import com.shadowsentinel.risk.dto.CreateAiDomainRequest;
import com.shadowsentinel.risk.dto.UpdateAiDomainRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AiDomainService {

    private static final Logger log = LoggerFactory.getLogger(AiDomainService.class);

    private final AiDomainRepository aiDomainRepository;

    public AiDomainService(AiDomainRepository aiDomainRepository) {
        this.aiDomainRepository = aiDomainRepository;
    }

    @Transactional
    public Optional<AiDomain> autoDiscoverDomain(String rawDomain) {
        if (rawDomain == null || rawDomain.isBlank()) {
            return Optional.empty();
        }

        String domain = rawDomain.trim().toLowerCase();

        if (aiDomainRepository.existsByDomain(domain)) {
            log.debug("Domain {} is already present in AI domain registry", domain);
            return Optional.empty();
        }

        Instant now = Instant.now();
        AiDomain aiDomain = AiDomain.builder()
                .domain(domain)
                .status(AiDomainStatus.UNKNOWN)
                .addedBy(null)
                .notes(null)
                .firstSeenAt(now)
                .updatedAt(now)
                .build();

        try {
            AiDomain saved = aiDomainRepository.save(aiDomain);
            log.info("Auto-discovered AI domain registered: {} with status UNKNOWN", domain);
            return Optional.of(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Domain {} was inserted concurrently", domain);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Page<AiDomainResponse> getAiDomains(AiDomainStatus status, Pageable pageable) {
        Page<AiDomain> page = (status != null)
                ? aiDomainRepository.findByStatus(status, pageable)
                : aiDomainRepository.findAll(pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional
    public AiDomainResponse createAiDomain(CreateAiDomainRequest request, User admin) {
        String domain = request.getDomain().trim().toLowerCase();

        if (aiDomainRepository.existsByDomain(domain)) {
            throw new DuplicateResourceException("AI domain already registered: " + domain);
        }

        Instant now = Instant.now();
        AiDomain aiDomain = AiDomain.builder()
                .domain(domain)
                .status(request.getStatus())
                .addedBy(admin != null ? admin.getEmail() : null)
                .notes(request.getNotes())
                .firstSeenAt(now)
                .updatedAt(now)
                .build();

        AiDomain saved = aiDomainRepository.save(aiDomain);
        log.info("Admin {} manually registered AI domain: {} with status {}",
                admin != null ? admin.getEmail() : "system", domain, request.getStatus());
        return mapToResponse(saved);
    }

    @Transactional
    public AiDomainResponse updateAiDomain(Long id, UpdateAiDomainRequest request, User admin) {
        AiDomain domain = aiDomainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI domain not found with id: " + id));

        if (request.getStatus() != null) {
            domain.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            domain.setNotes(request.getNotes());
        }

        domain.setAddedBy(admin != null ? admin.getEmail() : null);
        domain.setUpdatedAt(Instant.now());

        AiDomain updated = aiDomainRepository.save(domain);
        log.info("Admin {} updated AI domain {} (id: {})",
                admin != null ? admin.getEmail() : "system", domain.getDomain(), id);
        return mapToResponse(updated);
    }

    public AiDomainResponse mapToResponse(AiDomain domain) {
        return AiDomainResponse.builder()
                .id(domain.getId())
                .domain(domain.getDomain())
                .status(domain.getStatus())
                .addedBy(domain.getAddedBy())
                .notes(domain.getNotes())
                .firstSeenAt(domain.getFirstSeenAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
