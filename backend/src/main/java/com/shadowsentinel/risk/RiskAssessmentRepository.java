package com.shadowsentinel.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long>, JpaSpecificationExecutor<RiskAssessment> {

    Optional<RiskAssessment> findByActivityId(Long activityId);

    boolean existsByActivityId(Long activityId);
}
