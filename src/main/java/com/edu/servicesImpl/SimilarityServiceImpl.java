package com.edu.servicesImpl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.edu.dto.SimilarityMatch;
import com.edu.entity.DocumentHash;
import com.edu.repositories.DocumentHashRepository;
import com.edu.services.SimilarityMatchService;

@Service
public class SimilarityServiceImpl implements SimilarityMatchService {

    @Autowired
    private DocumentHashRepository documentHashRepository;

    @Value("${edublock.ai.similarity-threshold:0.85}")
    private double similarityThreshold;

    @Override
    public SimilarityMatch findClosestMatch(String pHash, String ocrText) {
        if (pHash == null || pHash.length() != 1024) {
            return new SimilarityMatch(null, 0.0, "No strong match");
        }

        // 1. Slice binary hash into 8 128-bit chunks
        String c0 = pHash.substring(0, 128);
        String c1 = pHash.substring(128, 256);
        String c2 = pHash.substring(256, 384);
        String c3 = pHash.substring(384, 512);
        String c4 = pHash.substring(512, 640);
        String c5 = pHash.substring(640, 768);
        String c6 = pHash.substring(768, 896);
        String c7 = pHash.substring(896, 1024);

        // 2. Fetch candidates from database using B-Tree index lookup
        List<DocumentHash> candidates = documentHashRepository.findCandidatesByChunks(c0, c1, c2, c3, c4, c5, c6, c7);

        double bestScore = 0.0;
        UUID bestId = null;

        // 3. Compute Hamming distance only on narrow candidate subset
        for (DocumentHash candidate : candidates) {
            String existingHash = candidate.getFullHash();
            if (existingHash == null || existingHash.length() != 1024) {
                continue;
            }
            int dist = hammingDistance(pHash, existingHash);
            double score = 1.0 - (dist / 1024.0);

            if (score > bestScore) {
                bestScore = score;
                bestId = candidate.getDocumentId();
            }
        }

        return new SimilarityMatch(
            bestId,
            bestScore,
            bestScore > similarityThreshold ? "High visual similarity" : "No strong match"
        );
    }

    @Override
    public SimilarityMatch findSimilar(String pHash) {
        SimilarityMatch match = findClosestMatch(pHash, null);
        if (match.getScore() > similarityThreshold) {
            return match;
        }
        return null;
    }

    private int hammingDistance(String a, String b) {
        int d = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                d++;
            }
        }
        return d;
    }
}
