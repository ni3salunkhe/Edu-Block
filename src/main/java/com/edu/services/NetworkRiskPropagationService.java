package com.edu.services;

import java.util.UUID;

public interface NetworkRiskPropagationService {
    void propagateRisk(UUID institutionId, int baseRisk);
}
