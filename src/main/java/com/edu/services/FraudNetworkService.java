package com.edu.services;

import com.edu.dto.FraudNetworkGraphDTO;
import com.edu.entity.FraudCluster;
import java.util.List;
import java.util.UUID;

public interface FraudNetworkService {
    void buildRelationships(UUID documentId);
    List<FraudCluster> executeConnectedComponentClustering();
    FraudNetworkGraphDTO getClusterGraph(UUID clusterId);
}
