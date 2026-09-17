package com.shadowsentinel.risk.event;

import com.shadowsentinel.risk.RiskAssessment;
import org.springframework.context.ApplicationEvent;

public class RiskAssessedEvent extends ApplicationEvent {

    private final RiskAssessment assessment;

    public RiskAssessedEvent(Object source, RiskAssessment assessment) {
        super(source);
        this.assessment = assessment;
    }

    public RiskAssessment getAssessment() {
        return assessment;
    }
}
