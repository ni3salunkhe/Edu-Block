package com.edu.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityMatch {

    private UUID documentId;     // closest matching doc
    private double score;        // similarity (0.0 – 1.0)
    private String reason;       // explanation
}

