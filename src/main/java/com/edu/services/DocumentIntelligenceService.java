package com.edu.services;

import com.edu.dto.DocumentIntelligenceDTO;
import java.util.UUID;

public interface DocumentIntelligenceService {
    
    void extractAndValidate(UUID documentId, String rawText);
    
    DocumentIntelligenceDTO getIntelligence(UUID documentId);
}
