package com.edu.servicesImpl;

import com.edu.dto.events.DocumentEvents.DocumentUploadedEvent;
import com.edu.dto.events.DocumentEvents.DocumentOcrCompletedEvent;
import com.edu.entity.Document;
import com.edu.entity.DocumentOcr;
import com.edu.entity.DocumentHash;
import com.edu.repositories.DocumentOcrRepository;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.DocumentHashRepository;
import com.edu.services.OCRService;
import com.edu.services.ImageHashService;
import com.edu.util.FileDownloadHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;

@Component
public class DocumentOcrListener {

    private final OCRService ocrService;
    private final ImageHashService imageHashService;
    private final DocumentRepository documentRepository;
    private final DocumentOcrRepository ocrRepository;
    private final DocumentHashRepository documentHashRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileDownloadHelper fileDownloadHelper;

    public DocumentOcrListener(OCRService ocrService, 
                               ImageHashService imageHashService,
                               DocumentRepository documentRepository,
                               DocumentOcrRepository ocrRepository, 
                               DocumentHashRepository documentHashRepository,
                               ApplicationEventPublisher eventPublisher,
                               FileDownloadHelper fileDownloadHelper) {
        this.ocrService = ocrService;
        this.imageHashService = imageHashService;
        this.documentRepository = documentRepository;
        this.ocrRepository = ocrRepository;
        this.documentHashRepository = documentHashRepository;
        this.eventPublisher = eventPublisher;
        this.fileDownloadHelper = fileDownloadHelper;
    }

    @Async
    @EventListener
    @Transactional
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        Document doc = documentRepository.findById(event.getDocumentId()).orElse(null);
        if (doc == null) return;

        DocumentOcr ocr = ocrRepository.findById(event.getDocumentId())
                .orElseGet(() -> {
                    DocumentOcr newOcr = new DocumentOcr();
                    newOcr.setDocument(doc);
                    newOcr.setOcrStatus(DocumentOcr.OcrStatus.PROCESSING);
                    return newOcr;
                });

        ocr.setLastAttemptAt(Instant.now());
        ocr.setRetryCount(ocr.getRetryCount() + 1);
        ocr.setOcrStatus(DocumentOcr.OcrStatus.PROCESSING);
        ocrRepository.save(ocr);

        File tempFile = null;
        try {
            long startTime = System.currentTimeMillis();
            tempFile = fileDownloadHelper.downloadFromUrl(doc.getFilePath());
            
            // 1. Run OCR
            String text = ocrService.extractText(tempFile);
            
            // 2. Generate and save Perceptual Hash
            String perceptualHash = computeHash(tempFile);
            doc.setPerceptualHash(perceptualHash);
            documentRepository.save(doc);

            // Save chunked hash
            saveDocumentHashChunks(doc, perceptualHash);

            long duration = System.currentTimeMillis() - startTime;

            ocr.setRawText(text);
            ocr.setOcrStatus(DocumentOcr.OcrStatus.SUCCESS);
            ocr.setFailureReason(null);
            ocr.setOcrMetadataJson(String.format(
                "{\"ocrEngine\":\"Tesseract\",\"ocrVersion\":\"5.3.0\",\"processingDurationMs\":%d}", duration
            ));
            ocr.setProcessedAt(Instant.now());
            ocrRepository.save(ocr);

            // Forward to metadata parsing stage
            eventPublisher.publishEvent(new DocumentOcrCompletedEvent(doc.getId(), text));
        } catch (Exception e) {
            handleOcrFailure(ocr, doc, e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String computeHash(File file) throws Exception {
        String perceptualHash = null;
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file)) {
                org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
                java.awt.image.BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, org.apache.pdfbox.rendering.ImageType.RGB);
                
                File tempImage = File.createTempFile("hash_gen_", ".png");
                try {
                    javax.imageio.ImageIO.write(bim, "png", tempImage);
                    perceptualHash = imageHashService.computePerceptualHash(tempImage);
                } finally {
                    if (tempImage.exists()) {
                        tempImage.delete();
                    }
                }
            }
        } else {
            perceptualHash = imageHashService.computePerceptualHash(file);
        }
        return perceptualHash;
    }

    private void saveDocumentHashChunks(Document doc, String perceptualHash) {
        if (perceptualHash == null || perceptualHash.length() != 1024) {
            return;
        }
        DocumentHash docHash = documentHashRepository.findById(doc.getId()).orElseGet(() -> {
            DocumentHash newHash = new DocumentHash();
            newHash.setDocument(doc);
            return newHash;
        });

        docHash.setChunk0(perceptualHash.substring(0, 128));
        docHash.setChunk1(perceptualHash.substring(128, 256));
        docHash.setChunk2(perceptualHash.substring(256, 384));
        docHash.setChunk3(perceptualHash.substring(384, 512));
        docHash.setChunk4(perceptualHash.substring(512, 640));
        docHash.setChunk5(perceptualHash.substring(640, 768));
        docHash.setChunk6(perceptualHash.substring(768, 896));
        docHash.setChunk7(perceptualHash.substring(896, 1024));
        docHash.setFullHash(perceptualHash);
        
        documentHashRepository.save(docHash);
    }

    private void handleOcrFailure(DocumentOcr ocr, Document doc, Exception e) {
        ocr.setLastAttemptAt(Instant.now());
        if (ocr.getRetryCount() >= 3) {
            ocr.setOcrStatus(DocumentOcr.OcrStatus.FAILED);
            ocr.setFailureReason("Failed after max retries: " + e.getMessage());
            ocrRepository.save(ocr);

            // Update document status to AI_REVIEW_REQUIRED
            doc.setStatus(Document.Status.AI_REVIEW_REQUIRED);
            documentRepository.save(doc);
        } else {
            ocr.setOcrStatus(DocumentOcr.OcrStatus.FAILED); // Eligible for scheduler retry
            ocr.setFailureReason("Attempt " + ocr.getRetryCount() + " failed: " + e.getMessage());
            ocrRepository.save(ocr);
        }
    }
}
