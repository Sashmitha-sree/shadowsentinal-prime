package com.shadowsentinel.classification;

import org.springframework.context.ApplicationEvent;

public class ClassificationCompletedEvent extends ApplicationEvent {

    private final ClassificationResult result;

    public ClassificationCompletedEvent(Object source, ClassificationResult result) {
        super(source);
        this.result = result;
    }

    public ClassificationResult getResult() {
        return result;
    }
}
