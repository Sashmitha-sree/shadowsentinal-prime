package com.shadowsentinel.classification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassificationEvidenceRepository extends JpaRepository<ClassificationEvidence, Long> {

    Optional<ClassificationEvidence> findByActivityId(Long activityId);

    boolean existsByActivityId(Long activityId);
}
