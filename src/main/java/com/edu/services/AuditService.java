package com.edu.services;

import java.util.UUID;

public interface AuditService {

    void logAdminAction(
        UUID adminId,
        String action,
        String targetType,
        UUID targetId,
        String metadata,
        String ipAddress
    );

    void logVerifierAction(
        UUID verifierId,
        String action,
        String targetType,
        UUID targetId,
        String metadata,
        String ipAddress
    );

    void logEmployerAction(
        UUID employerId,
        String action,
        String targetType,
        UUID targetId,
        String metadata,
        String ipAddress
    );

    void logIndividualAction(
        UUID individualId,
        String action,
        String targetType,
        UUID targetId,
        String metadata,
        String ipAddress
    );
    
}
