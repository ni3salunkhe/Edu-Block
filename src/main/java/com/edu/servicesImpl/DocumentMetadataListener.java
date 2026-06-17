package com.edu.servicesImpl;

import com.edu.dto.events.DocumentEvents.DocumentOcrCompletedEvent;
import com.edu.services.DocumentIntelligenceService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DocumentMetadataListener {

    private final DocumentIntelligenceService intelligenceService;

    public DocumentMetadataListener(DocumentIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @Async
    @EventListener
    public void handleOcrCompleted(DocumentOcrCompletedEvent event) {
        intelligenceService.extractAndValidate(event.getDocumentId(), event.getRawText());
    }
}
