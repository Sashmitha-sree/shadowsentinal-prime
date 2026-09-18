package com.shadowsentinel.risk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiDomainRepository extends JpaRepository<AiDomain, Long> {

    Optional<AiDomain> findByDomain(String domain);

    boolean existsByDomain(String domain);

    Page<AiDomain> findByStatus(AiDomainStatus status, Pageable pageable);
}
