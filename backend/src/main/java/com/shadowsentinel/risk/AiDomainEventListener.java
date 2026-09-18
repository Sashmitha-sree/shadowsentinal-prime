package com.shadowsentinel.risk;

import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.classification.ClassificationCompletedEvent;
import com.shadowsentinel.classification.ClassificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class AiDomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(AiDomainEventListener.class);

    private final AiDomainService aiDomainService;

    public AiDomainEventListener(AiDomainService aiDomainService) {
        this.aiDomainService = aiDomainService;
    }

    @Order(1)
    @EventListener
    public void onClassificationCompleted(ClassificationCompletedEvent event) {
        if (event == null || event.getResult() == null) {
            return;
        }

        ClassificationResult result = event.getResult();
        if (result.getClassLabel() != null && result.getClassLabel() != ClassLabel.NON_AI) {
            try {
                if (result.getActivity() != null && result.getActivity().getDomain() != null) {
                    aiDomainService.autoDiscoverDomain(result.getActivity().getDomain());
                }
            } catch (Exception ex) {
                log.error("Failed to process AI domain auto-discovery for activity {}: {}",
                        result.getActivity() != null ? result.getActivity().getId() : "unknown",
                        ex.getMessage(), ex);
            }
        }
    }
}
