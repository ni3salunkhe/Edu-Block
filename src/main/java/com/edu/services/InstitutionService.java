package com.edu.services;

import java.util.List;
import java.util.UUID;

import com.edu.dto.CreateInstitutionRequest;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionVerificationAudit;

public interface InstitutionService {
    Institution createInstitution(CreateInstitutionRequest request);
    Institution getInstitution(UUID id);
    List<Institution> listInstitutions();
    Institution verifyInstitution(UUID institutionId, String reason, UUID adminId);
    Institution accreditInstitution(UUID institutionId, String reason, UUID adminId);
    List<InstitutionVerificationAudit> getAuditHistory(UUID institutionId);
}

