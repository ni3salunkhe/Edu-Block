package com.edu.services;

import com.edu.entity.DocumentIntelligence;

public interface MetadataExtractor {
    
    DocumentIntelligence.DocumentClassification classify(String ocrText);
    
    void extractMetadata(String ocrText, DocumentIntelligence target);
}
