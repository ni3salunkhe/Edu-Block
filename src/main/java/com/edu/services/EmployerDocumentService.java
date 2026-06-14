package com.edu.services;

import com.edu.dto.EmployerDocumentViewDTO;
import com.edu.dto.DocumentTrustResponseDTO;
import java.util.List;
import java.util.UUID;

public interface EmployerDocumentService {
    List<EmployerDocumentViewDTO> getSharedDocuments(UUID employerId, String ipAddress);
    EmployerDocumentViewDTO getDocumentDetails(UUID employerId, UUID documentId, String ipAddress);
    DocumentTrustResponseDTO getDocumentTrustDetails(UUID employerId, UUID documentId, String ipAddress);
}
