package com.edu.servicesImpl;

import com.edu.dto.events.CredentialEvents.CredentialIssuedEvent;
import com.edu.dto.events.InstitutionEvents.InstitutionAccreditationChangedEvent;
import com.edu.dto.events.VerificationEvents.DocumentVerificationCompletedEvent;
import com.edu.dto.events.VerificationEvents.FraudSignalDetectedEvent;
import com.edu.services.InstitutionTrustService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TrustGraphEventListener {

    private final InstitutionTrustService trustService;

    public TrustGraphEventListener(InstitutionTrustService trustService) {
        this.trustService = trustService;
    }

    @Async
    @EventListener
    public void handleVerificationCompleted(DocumentVerificationCompletedEvent event) {
        if (event.getInstitutionId() != null) {
            trustService.handleVerificationSuccess(event.getInstitutionId(), event.getConfidenceScore());
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
