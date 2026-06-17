package com.edu.services;

import com.edu.dto.EvidenceSnapshotDTO;
import java.util.UUID;

public interface EvidenceAggregationService {
    EvidenceSnapshotDTO aggregateEvidence(UUID documentId);
}
