package com.edu.services;

import com.edu.entity.Institution;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface VerificationContext {
    
    Optional<Institution> getResolvedInstitution();
    
    long getReferenceReuseCount(String credentialReference);
    
    boolean hasAccreditation(UUID institutionId);
    
    Map<String, Object> getExtendedData();
}
