package com.edu.servicesImpl;

import org.springframework.security.access.AccessDeniedException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.edu.entity.Document;
import com.edu.services.ConsentService;
import com.edu.services.DocumentAccessService;
import com.edu.util.SecurityUtil;

@Service
public class DocumentAccessServiceImpl implements DocumentAccessService {

    private final ConsentService consentService;

    public DocumentAccessServiceImpl(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public void assertCanAccess(Document doc) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();

        // OWNER
        if (doc.getOwnerId().equals(currentUserId)) {
            return;
        }

        // VERIFIER
        if ("VERIFIER".equals(role)) {
            return;
        }

        // EMPLOYER WITH CONSENT
        if ("EMPLOYER".equals(role) &&
            consentService.hasActiveConsent(currentUserId, doc.getOwnerId())) {
            return;
        }

        throw new AccessDeniedException("Access denied to document");
    }
}
