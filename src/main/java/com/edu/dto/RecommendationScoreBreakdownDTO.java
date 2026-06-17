package com.edu.dto;

import java.io.Serializable;

public class RecommendationScoreBreakdownDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int trustContribution;
    private int verificationContribution;
    private int fraudContribution;
    private int riskContribution;
    private int blockchainContribution;

    public RecommendationScoreBreakdownDTO() {}

    public RecommendationScoreBreakdownDTO(int trustContribution, int verificationContribution, int fraudContribution, int riskContribution, int blockchainContribution) {
        this.trustContribution = trustContribution;
        this.verificationContribution = verificationContribution;
        this.fraudContribution = fraudContribution;
        this.riskContribution = riskContribution;
        this.blockchainContribution = blockchainContribution;
    }

    public int getTrustContribution() {
        return trustContribution;
    }

    public void setTrustContribution(int trustContribution) {
        this.trustContribution = trustContribution;
    }

    public int getVerificationContribution() {
        return verificationContribution;
    }

    public void setVerificationContribution(int verificationContribution) {
        this.verificationContribution = verificationContribution;
    }

    public int getFraudContribution() {
        return fraudContribution;
    }

    public void setFraudContribution(int fraudContribution) {
        this.fraudContribution = fraudContribution;
    }

    public int getRiskContribution() {
        return riskContribution;
    }

    public void setRiskContribution(int riskContribution) {
        this.riskContribution = riskContribution;
    }

    public int getBlockchainContribution() {
        return blockchainContribution;
    }

    public void setBlockchainContribution(int blockchainContribution) {
        this.blockchainContribution = blockchainContribution;
    }
}
