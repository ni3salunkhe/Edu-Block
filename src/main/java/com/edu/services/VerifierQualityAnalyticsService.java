package com.edu.services;

import com.edu.entity.VerifierQualityMetrics;
import java.time.Instant;
import java.util.List;

public interface VerifierQualityAnalyticsService {
    List<VerifierQualityMetrics> calculateVerifierMetrics(Instant start, Instant end);
}
