package com.shadowsentinel;

import com.shadowsentinel.alert.AlertRepository;
import com.shadowsentinel.audit.AuditLogRepository;
import com.shadowsentinel.auth.UserRepository;
import com.shadowsentinel.browser.BrowserActivityRepository;
import com.shadowsentinel.browser.BrowserSessionRepository;
import com.shadowsentinel.classification.ClassificationEvidenceRepository;
import com.shadowsentinel.classification.ClassificationResultRepository;
import com.shadowsentinel.risk.RiskAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestDatabaseCleaner {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private ClassificationResultRepository resultRepository;

    @Autowired
    private ClassificationEvidenceRepository evidenceRepository;

    @Autowired
    private BrowserActivityRepository activityRepository;

    @Autowired
    private BrowserSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void clean() {
        alertRepository.deleteAll();
        riskAssessmentRepository.deleteAll();
        resultRepository.deleteAll();
        evidenceRepository.deleteAll();
        activityRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();
    }
}
