package com.shadowsentinel.classification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassificationResultRepository extends JpaRepository<ClassificationResult, Long>, JpaSpecificationExecutor<ClassificationResult> {

    Optional<ClassificationResult> findByActivityId(Long activityId);
}
