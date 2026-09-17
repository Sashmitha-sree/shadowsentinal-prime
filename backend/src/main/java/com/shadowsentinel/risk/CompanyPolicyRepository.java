package com.shadowsentinel.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyPolicyRepository extends JpaRepository<CompanyPolicy, Long> {

    Optional<CompanyPolicy> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<CompanyPolicy> findFirstByCompanyIdIsNullAndActiveTrueOrderByVersionDesc();

    List<CompanyPolicy> findAllByOrderByCreatedAtDesc();

    boolean existsByActiveTrue();
}
