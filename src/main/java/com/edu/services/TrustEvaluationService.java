package com.edu.services;

import com.edu.dto.DocumentTrustResponseDTO;
import com.edu.entity.Document;
import com.edu.entity.Institution;
import java.util.List;

public interface TrustEvaluationService {
    DocumentTrustResponseDTO evaluateDocument(Document document, Institution institution);
    List<DocumentTrustResponseDTO> evaluateDocuments(List<Document> documents);
}
