package com.edu.services;

import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.entity.ReviewerRecommendation;
import java.util.List;

public interface RecommendationExplanationBuilder {
    List<String> buildExplanations(ReviewerRecommendation.RecommendationType recommendation, EvidenceSnapshotDTO snapshot);
}
