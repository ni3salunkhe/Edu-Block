package com.edu.services;

import com.edu.entity.InstitutionBehavioralTrend;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InstitutionBehavioralLearningService {
    List<InstitutionBehavioralTrend> analyzeTrends(Instant start, Instant end);
    void triggerAdaptiveTrustCalibration(UUID institutionId);
}
