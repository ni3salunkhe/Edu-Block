package com.edu.servicesImpl;

import com.edu.dto.events.CredentialEvents.CredentialIssuedEvent;
import com.edu.dto.events.InstitutionEvents.InstitutionAccreditationChangedEvent;
import com.edu.dto.events.VerificationEvents.DocumentVerificationCompletedEvent;
import com.edu.dto.events.VerificationEvents.FraudSignalDetectedEvent;
import com.edu.services.InstitutionTrustService;
import com.edu.services.ReviewerCopilotService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TrustGraphEventListener {

    private final InstitutionTrustService trustService;
    private final ReviewerCopilotService reviewerCopilotService;

    public TrustGraphEventListener(InstitutionTrustService trustService, ReviewerCopilotService reviewerCopilotService) {
        this.trustService = trustService;
        this.reviewerCopilotService = reviewerCopilotService;
    }

    @Async
    @EventListener
    public void handleVerificationCompleted(DocumentVerificationCompletedEvent event) {
        if (event.getInstitutionId() != null) {
            trustService.handleVerificationSuccess(event.getInstitutionId(), event.getConfidenceScore());
        }
        if (event.getDocumentId() != null) {
            reviewerCopilotService.generateRecommendation(event.getDocumentId());
        }
    }

    @Async
    @EventListener
    public void handleFraudSignalDetected(FraudSignalDetectedEvent event) {
        if (event.getInstitutionId() != null) {
            trustService.handleFraudSignal(event.getInstitutionId(), event.getSignalType(), event.getSeverity());
        }
    }

    @Async
    @EventListener
    public void handleAccreditationChanged(InstitutionAccreditationChangedEvent event) {
        if (event.getInstitutionId() != null) {
            trustService.handleAccreditationChange(event.getInstitutionId(), event.getNewLevel());
        }
    }

    @Async
    @EventListener
    public void handleCredentialIssued(CredentialIssuedEvent event) {
        if (event.getInstitutionId() != null) {
            trustService.handleCredentialIssued(event.getInstitutionId());
        }
    }
}
