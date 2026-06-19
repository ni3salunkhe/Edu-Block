package com.edu.services;

import java.time.Instant;

public interface LearningDatasetExportService {
    String exportSignalsAsCsv(Instant start, Instant end);
    String exportSignalsAsJson(Instant start, Instant end);
    String exportRulePerformanceAsCsv();
    String exportRulePerformanceAsJson();
    String exportTrendsAsCsv();
    String exportTrendsAsJson();
}
