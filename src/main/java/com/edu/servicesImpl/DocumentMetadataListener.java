package com.edu.servicesImpl;

import com.edu.dto.events.DocumentEvents.DocumentOcrCompletedEvent;
import com.edu.services.DocumentIntelligenceService;
import com.edu.services.DocumentVerificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DocumentMetadataListener {

    private final DocumentIntelligenceService intelligenceService;
    private final DocumentVerificationService documentVerificationService;

    public DocumentMetadataListener(DocumentIntelligenceService intelligenceService,
                                    DocumentVerificationService documentVerificationService) {
        this.intelligenceService = intelligenceService;
        this.documentVerificationService = documentVerificationService;
    }

    @Async
    @EventListener
    public void handleOcrCompleted(DocumentOcrCompletedEvent event) {
        intelligenceService.extractAndValidate(event.getDocumentId(), event.getRawText());
        documentVerificationService.verifyDocument(event.getDocumentId());
    }
}
