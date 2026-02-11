package com.edu.services;

import java.util.UUID;

public interface ConsentService {

    boolean hasActiveConsent(UUID employerId, UUID ownerId);

    void requestConsent(UUID employerId, UUID ownerId);

    void approveConsent(UUID ownerId, UUID employerId);

    void revokeConsent(UUID ownerId, UUID employerId);

    // New methods for frontend alignment
    com.edu.dto.ConsentViewModel getConsents(UUID ownerId);

    void approveConsentById(UUID consentId, UUID ownerId);

    void rejectConsentById(UUID consentId, UUID ownerId);

    void revokeConsentById(UUID consentId, UUID ownerId);
}
