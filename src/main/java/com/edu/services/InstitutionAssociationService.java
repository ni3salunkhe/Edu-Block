package com.edu.services;

import java.util.List;
import java.util.UUID;

import com.edu.entity.InstitutionAssociation;

public interface InstitutionAssociationService {
    InstitutionAssociation requestAssociation(UUID studentId, UUID institutionId, String enrollmentNumber);
    InstitutionAssociation approveAssociation(UUID associationId, UUID institutionUserId);
    InstitutionAssociation rejectAssociation(UUID associationId, UUID institutionUserId, String reason);
    InstitutionAssociation revokeAssociation(UUID associationId, UUID institutionUserId, String reason);
    List<InstitutionAssociation> getStudentAssociations(UUID studentId);
    List<InstitutionAssociation> getInstitutionAssociations(UUID institutionId);
}
