package com.edu.services;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.edu.dto.InstitutionIssuedDocumentDTO;
import com.edu.entity.Document;

public interface InstitutionCredentialService {
    InstitutionIssuedDocumentDTO issueDocument(UUID institutionUserId, UUID studentId, UUID associationId,
                           String documentType, String credentialReference, MultipartFile file);
    List<InstitutionIssuedDocumentDTO> getIssuedDocuments(UUID institutionUserId);
}
