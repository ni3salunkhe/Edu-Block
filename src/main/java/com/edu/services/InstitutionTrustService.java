package com.edu.services;

import com.edu.entity.InstitutionTrustProfile;
import java.util.UUID;

public interface InstitutionTrustService {
    
    InstitutionTrustProfile getOrCreateProfile(UUID institutionId);
    
    InstitutionTrustProfile recalculateTrustProfile(UUID institutionId, String changeTriggerType, String reason);
    
    void handleVerificationSuccess(UUID institutionId, int confidenceScore);
    
    void handleFraudSignal(UUID institutionId, String signalType, String severity);

    void handleAccreditationChange(UUID institutionId, String newLevel);

    void handleCredentialIssued(UUID institutionId);
}
