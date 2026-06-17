package com.edu.repositories.projections;

public interface InstitutionMetricsProjection {
    Long getTotalIssued();
    Long getTotalVerified();
    Double getAvgConfidence();
    Long getTotalSignals();
}
