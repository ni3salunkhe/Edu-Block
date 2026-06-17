package com.edu.dto;

import java.io.Serializable;
import java.time.Instant;

public class TrustScoreBreakdownDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int accreditationContribution;
    private int verificationContribution;
    private int fraudPenalty;
    private int riskPenalty;
    private int finalScore;
    private Instant trustScoreGeneratedAt;

    public TrustScoreBreakdownDTO() {}

    public TrustScoreBreakdownDTO(int accreditationContribution, int verificationContribution, int fraudPenalty, int riskPenalty, int finalScore, Instant trustScoreGeneratedAt) {
        this.accreditationContribution = accreditationContribution;
        this.verificationContribution = verificationContribution;
        this.fraudPenalty = fraudPenalty;
        this.riskPenalty = riskPenalty;
        this.finalScore = finalScore;
        this.trustScoreGeneratedAt = trustScoreGeneratedAt;
    }

    public int getAccreditationContribution() {
        return accreditationContribution;
    }

    public void setAccreditationContribution(int accreditationContribution) {
        this.accreditationContribution = accreditationContribution;
    }

    public int getVerificationContribution() {
        return verificationContribution;
    }

    public void setVerificationContribution(int verificationContribution) {
        this.verificationContribution = verificationContribution;
    }

    public int getFraudPenalty() {
        return fraudPenalty;
    }

    public void setFraudPenalty(int fraudPenalty) {
        this.fraudPenalty = fraudPenalty;
    }

    public int getRiskPenalty() {
        return riskPenalty;
    }

    public void setRiskPenalty(int riskPenalty) {
        this.riskPenalty = riskPenalty;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public Instant getTrustScoreGeneratedAt() {
        return trustScoreGeneratedAt;
    }

    public void setTrustScoreGeneratedAt(Instant trustScoreGeneratedAt) {
        this.trustScoreGeneratedAt = trustScoreGeneratedAt;
    }
}
