package com.shadowsentinel.browser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrowserSessionRepository extends JpaRepository<BrowserSession, Long> {

    Page<BrowserSession> findByUserId(Long userId, Pageable pageable);

    Optional<BrowserSession> findByIdAndUserId(Long id, Long userId);
}
