package com.shadowsentinel.risk;

import com.shadowsentinel.classification.ClassificationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RiskAssessmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentEventListener.class);

    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentEventListener(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @EventListener
    public void onClassificationCompleted(ClassificationCompletedEvent event) {
        if (event == null || event.getResult() == null) {
            return;
        }

        try {
            riskAssessmentService.assessClassification(event.getResult());
        } catch (Exception ex) {
            log.error("Failed to evaluate risk assessment for activity {}: {}",
                    event.getResult().getActivity() != null ? event.getResult().getActivity().getId() : "unknown",
                    ex.getMessage(), ex);
        }
    }
}
