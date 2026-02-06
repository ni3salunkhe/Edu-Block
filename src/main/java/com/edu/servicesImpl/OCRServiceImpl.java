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
        this.tesseract.setDatapath("tessdata"); // local tessdata folder
        this.tesseract.setLanguage("eng");
    }

    @Override
    public String extractText(File file) {
        try {
            return tesseract.doOCR(file);
        } catch (Exception e) {
            throw new RuntimeException("OCR failed", e);
        }
    }
}

