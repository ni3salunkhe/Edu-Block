package com.edu.dto;

public class RulePerformanceDTO {
    private String ruleName;
    private String targetType;
    private Long totalEvaluations;
    private Long overrideCount;
    private Long truePositives;
    private Long falsePositives;
    private Long falseNegatives;
    private Double precisionRate;
    private Double overrideRate;

    public RulePerformanceDTO() {}

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTotalEvaluations() {
        return totalEvaluations;
    }

    public void setTotalEvaluations(Long totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }

    public Long getOverrideCount() {
        return overrideCount;
    }

    public void setOverrideCount(Long overrideCount) {
        this.overrideCount = overrideCount;
    }

    public Long getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(Long truePositives) {
        this.truePositives = truePositives;
    }

    public Long getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(Long falsePositives) {
        this.falsePositives = falsePositives;
    }

    public Long getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(Long falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public Double getPrecisionRate() {
        return precisionRate;
    }

    public void setPrecisionRate(Double precisionRate) {
        this.precisionRate = precisionRate;
    }

    public Double getOverrideRate() {
        return overrideRate;
    }

    public void setOverrideRate(Double overrideRate) {
        this.overrideRate = overrideRate;
    }
}
