package com.edu.servicesImpl;

import java.io.File;

import org.springframework.stereotype.Service;

import com.edu.services.OCRService;

import net.sourceforge.tess4j.Tesseract;

@Service
public class OCRServiceImpl implements OCRService {

    private final Tesseract tesseract;

    public OCRServiceImpl() {
        this.tesseract = new Tesseract();
        // Use system-installed tessdata path (set by apt-get install tesseract-ocr-eng in Docker).
        // Falls back to a local 'tessdata' folder for development environments.
        String systemTessdata = "/usr/share/tesseract-ocr/5/tessdata";
        String localTessdata = "tessdata";
        java.io.File systemPath = new java.io.File(systemTessdata);
        this.tesseract.setDatapath(systemPath.exists() ? systemTessdata : localTessdata);
        this.tesseract.setLanguage("eng");
    }

    @Override
    public String extractText(File file) {
        try {
            return tesseract.doOCR(file);
        } catch (Exception e) {
            // Non-fatal: log the failure and return empty text so uploads are not blocked.
            System.err.println("[OCR] Warning: OCR extraction failed (non-fatal): " + e.getMessage());
            return "";
        }
    }
}

