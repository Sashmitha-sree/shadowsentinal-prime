package com.shadowsentinel.alert;

import com.shadowsentinel.risk.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    Optional<Alert> findFirstByUserIdAndActivityDomainAndSeverityAndStatusNotAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long userId, String domain, Severity severity, AlertStatus status, Instant cutoff);
}
