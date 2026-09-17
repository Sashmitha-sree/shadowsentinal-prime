package com.shadowsentinel.alert;

import com.shadowsentinel.risk.event.RiskAssessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlertEventListener {

    private static final Logger log = LoggerFactory.getLogger(AlertEventListener.class);

    private final AlertService alertService;

    public AlertEventListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @EventListener
    public void onRiskAssessed(RiskAssessedEvent event) {
        if (event == null || event.getAssessment() == null) {
            return;
        }

        try {
            alertService.processRiskAssessment(event.getAssessment());
        } catch (Exception ex) {
            log.error("Failed to process risk assessment for alert trigger: {}", ex.getMessage(), ex);
        }
    }
}
