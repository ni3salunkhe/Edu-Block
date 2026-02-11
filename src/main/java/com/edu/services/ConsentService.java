package com.edu.services;

import java.util.UUID;

public interface ConsentService {

    boolean hasActiveConsent(UUID employerId, UUID ownerId);

    void requestConsent(UUID employerId, UUID ownerId);

    void approveConsent(UUID ownerId, UUID employerId);

    void revokeConsent(UUID ownerId, UUID employerId);
}
