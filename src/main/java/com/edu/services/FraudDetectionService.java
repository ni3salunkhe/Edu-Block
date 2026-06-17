package com.edu.services;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import java.util.List;

public interface FraudDetectionService {
    
    List<FraudSignal> detectSignals(Document doc, DocumentIntelligence intel, DocumentOcr ocr, VerificationContext context);
}
