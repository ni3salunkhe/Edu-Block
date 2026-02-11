package com.edu.servicesImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.edu.dto.AuditLogDTO;
import com.edu.entity.AuditLog;
import com.edu.repositories.AuditLogRepository;
import com.edu.services.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditRepo;

    public AuditServiceImpl(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    private void log(UUID actorId,
                     AuditLog.ActorRole role,
                     String action,
                     String targetType,
                     UUID targetId,
                     String metadata,
                     String ip) {

        AuditLog log = new AuditLog();
        log.setActorUserId(actorId);
        log.setActorRole(role);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setMetadata(metadata);
        log.setIpAddress(ip);
        log.setCreatedAt(Instant.now());

        auditRepo.save(log);
    }

    @Override
    public void logAdminAction(UUID adminId,
                               String action,
                               String targetType,
                               UUID targetId,
                               String metadata,
                               String ipAddress) {
        log(adminId, AuditLog.ActorRole.ADMIN, action, targetType, targetId, metadata, ipAddress);
    }

    @Override
    public void logVerifierAction(UUID verifierId,
                                  String action,
                                  String targetType,
                                  UUID targetId,
                                  String metadata,
                                  String ipAddress) {
        log(verifierId, AuditLog.ActorRole.VERIFIER, action, targetType, targetId, metadata, ipAddress);
    }

    @Override
    public void logEmployerAction(UUID employerId,
                                  String action,
                                  String targetType,
                                  UUID targetId,
                                  String metadata,
                                  String ipAddress) {
        log(employerId, AuditLog.ActorRole.EMPLOYER, action, targetType, targetId, metadata, ipAddress);
    }

    @Override
    public void logIndividualAction(UUID individualId,
                                    String action,
                                    String targetType,
                                    UUID targetId,
                                    String metadata,
                                    String ipAddress) {
        log(individualId, AuditLog.ActorRole.INDIVIDUAL, action, targetType, targetId, metadata, ipAddress);
    }

    @Override
    public List<AuditLogDTO> getAllLogs() {
        return auditRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getCreatedAt(),
                        log.getActorRole() + " - " + log.getActorUserId(),
                        log.getAction(),
                        log.getTargetType() + " - " + log.getTargetId()
                ))
                .collect(Collectors.toList());
    }
}
