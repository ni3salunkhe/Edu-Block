package com.edu.servicesImpl;

import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.entity.ReviewerRecommendation;
import com.edu.services.RecommendationExplanationBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationExplanationBuilderImpl implements RecommendationExplanationBuilder {

    @Override
    public List<String> buildExplanations(ReviewerRecommendation.RecommendationType recommendation, EvidenceSnapshotDTO snapshot) {
        List<String> explanations = new ArrayList<>();

        // 1. Contradiction Warn
        boolean hasHighTrust = "VERY_HIGH".equalsIgnoreCase(snapshot.getInstitutionTrustTier()) || "HIGH".equalsIgnoreCase(snapshot.getInstitutionTrustTier());
        boolean hasLowTrust = "CRITICAL".equalsIgnoreCase(snapshot.getInstitutionTrustTier()) || "LOW".equalsIgnoreCase(snapshot.getInstitutionTrustTier());
        boolean hasDuplicate = snapshot.isHasDuplicateSimilarity();

        // 2. Recommendation Type specific summary
        if (recommendation == ReviewerRecommendation.RecommendationType.APPROVE) {
            explanations.add("✓ Recommended Action: APPROVE based on solid trust signals and validation success.");
            explanations.add("✓ Institution: " + snapshot.getInstitutionName() + " (" + snapshot.getInstitutionTrustTier() + " Trust)");
            if (snapshot.isBlockchainAnchored()) {
                explanations.add("✓ Blockchain verification confirmed (anchored successfully).");
            }
            if (snapshot.getOcrWordConfidence() >= 85.0) {
                explanations.add("✓ High text recognition confidence: " + String.format("%.1f", snapshot.getOcrWordConfidence()) + "%");
            }
        } else if (recommendation == ReviewerRecommendation.RecommendationType.REJECT) {
            explanations.add("✗ Recommended Action: REJECT due to severe integrity or trust violations.");
            if (hasLowTrust) {
                explanations.add("✗ Issuing Institution is flagged as " + snapshot.getInstitutionTrustTier() + " trust (Score: " + snapshot.getInstitutionTrustScore() + ").");
            }
            if (hasDuplicate) {
                explanations.add("✗ Duplicate document similarity check failed. A similar document already exists in repository.");
            }
            if (snapshot.getActiveFraudSignals() != null && !snapshot.getActiveFraudSignals().isEmpty()) {
                for (String signal : snapshot.getActiveFraudSignals()) {
                    explanations.add("✗ Active Fraud Signal: " + signal);
                }
            }
        } else {
            explanations.add("⚠ Recommended Action: REVIEW due to mixed or incomplete validation signals.");
            if (snapshot.isHasActiveRiskEvents()) {
                explanations.add("⚠ Active risk events logged against " + snapshot.getInstitutionName() + ".");
            }
            if (snapshot.getOcrWordConfidence() < 75.0) {
                explanations.add("⚠ Moderately low OCR confidence detected: " + String.format("%.1f", snapshot.getOcrWordConfidence()) + "%");
            }
            if (snapshot.getActiveFraudSignals() != null && !snapshot.getActiveFraudSignals().isEmpty()) {
                for (String signal : snapshot.getActiveFraudSignals()) {
                    explanations.add("⚠ Active Fraud Signal: " + signal);
                }
            }
        }

        // 3. Contradictory check
        boolean hasVerificationAnomaly = snapshot.getVerificationExplanations() != null &&
                snapshot.getVerificationExplanations().stream().anyMatch(e -> e.contains("-"));
        
        if (hasHighTrust && hasVerificationAnomaly && recommendation == ReviewerRecommendation.RecommendationType.REVIEW) {
            explanations.add("⚠ Mixed signals detected: The document has verification anomalies, but the issuing institution is highly trusted.");
        } else if (hasLowTrust && !hasVerificationAnomaly && recommendation == ReviewerRecommendation.RecommendationType.REVIEW) {
            explanations.add("⚠ Mixed signals detected: The document passes standard validation rules, but the issuing institution has a poor trust profile.");
        }

        return explanations;
    }
}
