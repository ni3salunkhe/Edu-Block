package com.edu.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIResult {

    /** Perceptual hash for visual similarity */
    private String perceptualHash;

    /** Similarity score with closest existing document (0.0 – 1.0) */
    private double similarityScore;

    /** ID of the most similar existing document (if any) */
    private UUID matchedDocumentId;

    /** OCR-extracted text (optional, can be truncated) */
    private String extractedText;

    /** AI confidence that document is genuine (0–100) */
    private int confidenceScore;

    /** Whether AI flags this document as suspicious */
    private boolean suspicious;

    /** Reason for flagging (human-readable) */
    private String flagReason;
}

