package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.DocumentVerificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentVerificationBackfillRunner {

    private final DocumentRepository documentRepository;
    private final DocumentVerificationService verificationService;

    public DocumentVerificationBackfillRunner(DocumentRepository documentRepository,
                                              DocumentVerificationService verificationService) {
        this.documentRepository = documentRepository;
        this.verificationService = verificationService;
    }

    @Async
    public void executeBackfill(int batchSize, long sleepBetweenBatchesMs) {
        List<UUID> docIds;
        do {
            docIds = documentRepository.findDocIdsWithoutVerification(PageRequest.of(0, batchSize));
            for (UUID id : docIds) {
                try {
                    verificationService.verifyDocument(id);
                } catch (Exception ignored) {}
            }

            if (!docIds.isEmpty()) {
                try {
                    Thread.sleep(sleepBetweenBatchesMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (!docIds.isEmpty());
    }
}
