package com.edu.dto;

import com.edu.entity.FraudSignal.FraudSignalType;
import java.util.Map;

public class VerificationStatsDTO {
    
    private long totalVerified;
    private long totalFlagged;
    private double averageConfidenceScore;
    private Map<FraudSignalType, Long> fraudSignalCounts;

    public VerificationStatsDTO() {}

    public VerificationStatsDTO(long totalVerified, long totalFlagged, double averageConfidenceScore, Map<FraudSignalType, Long> fraudSignalCounts) {
        this.totalVerified = totalVerified;
        this.totalFlagged = totalFlagged;
        this.averageConfidenceScore = averageConfidenceScore;
        this.fraudSignalCounts = fraudSignalCounts;
    }

    public long getTotalVerified() {
        return totalVerified;
    }

    public void setTotalVerified(long totalVerified) {
        this.totalVerified = totalVerified;
    }

    public long getTotalFlagged() {
        return totalFlagged;
    }

    public void setTotalFlagged(long totalFlagged) {
        this.totalFlagged = totalFlagged;
    }

    public double getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(double averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public Map<FraudSignalType, Long> getFraudSignalCounts() {
        return fraudSignalCounts;
    }

    public void setFraudSignalCounts(Map<FraudSignalType, Long> fraudSignalCounts) {
        this.fraudSignalCounts = fraudSignalCounts;
    }
}
