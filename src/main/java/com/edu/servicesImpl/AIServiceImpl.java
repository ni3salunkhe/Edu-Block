package com.edu.servicesImpl;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edu.dto.AIResult;
import com.edu.dto.SimilarityMatch;
import com.edu.services.AIService;
import com.edu.services.ImageHashService;
import com.edu.services.OCRService;
import com.edu.services.SimilarityMatchService;

@Service
public class AIServiceImpl implements AIService{
	
	@Autowired
	OCRService ocrService;
	
	@Autowired
	ImageHashService imageHashService;
	
	@Autowired
	SimilarityMatchService similarityService;
	
	@Override
    public AIResult analyze(File file) {

        String ocrText = ocrService.extractText(file);

        String perceptualHash = null;
        try {
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                // Convert first page to image for hashing
                try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file)) {
                    org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
                    java.awt.image.BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, org.apache.pdfbox.rendering.ImageType.RGB);
                    
                    File tempImage = File.createTempFile("hash_gen_", ".png");
                    javax.imageio.ImageIO.write(bim, "png", tempImage);
                    
                    perceptualHash = imageHashService.computePerceptualHash(tempImage);
                    
                    // Cleanup
                    tempImage.delete();
                }
            } else {
                perceptualHash = imageHashService.computePerceptualHash(file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating perceptual hash", e);
        }

        SimilarityMatch match =
            similarityService.findClosestMatch(perceptualHash, ocrText);

        boolean suspicious = match.getScore() > 0.85;

        return new AIResult(
            perceptualHash,
            match.getScore(),
            match.getDocumentId(),
            ocrText,
            suspicious ? 65 : 90,
            suspicious,
            suspicious ? "High similarity with existing document" : null
        );
    }

}
