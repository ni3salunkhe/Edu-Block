package com.edu.services;

import com.edu.dto.SimilarityMatch;

public interface SimilarityMatchService {
    SimilarityMatch findClosestMatch(String pHash, String ocrText);
    SimilarityMatch findSimilar(String perceptualHash);
}

