package com.edu.servicesImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edu.dto.SimilarityMatch;
import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.SimilarityMatchService;

@Service
public class SimilarityServiceImpl implements SimilarityMatchService {

    @Autowired
    private DocumentRepository documentRepo;

    @Override
    public SimilarityMatch findClosestMatch(String pHash, String ocrText) {

        List<Document> docs = documentRepo.findAll();

        double bestScore = 0;
        UUID bestId = null;

        for (Document d : docs) {
            String existingHash = d.getPerceptualHash();
            if (existingHash == null || pHash == null || existingHash.length() != pHash.length()) {
                continue;
            }
            int dist = hammingDistance(pHash, existingHash);
            double score = 1.0 - (dist / (double) pHash.length());

            if (score > bestScore) {
                bestScore = score;
                bestId = d.getId();
            }
        }

        return new SimilarityMatch(
            bestId,
            bestScore,
            bestScore > 0.85 ? "High visual similarity" : "No strong match"
        );
    }

    @Override
    public SimilarityMatch findSimilar(String pHash) {

        List<Document> existing = documentRepo.findByPerceptualHashIsNotNull();

        for (Document doc : existing) {
            String existingHash = doc.getPerceptualHash();
            if (existingHash == null || pHash == null || existingHash.length() != pHash.length()) continue;

            int dist = hammingDistance(pHash, existingHash);
            double similarity = 1.0 - (dist / (double) pHash.length());

            if (similarity > 0.85) {
                return new SimilarityMatch(doc.getId(), similarity,
                        "Visually similar document found");
            }
        }
        return null;
    }

    private int hammingDistance(String a, String b) {
        int d = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) d++;
        }
        return d;
    }
}
