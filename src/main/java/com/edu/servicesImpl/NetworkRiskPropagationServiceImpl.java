package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.NetworkRiskPropagationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class NetworkRiskPropagationServiceImpl implements NetworkRiskPropagationService {

    private final FraudNetworkNodeRepository nodeRepository;
    private final FraudNetworkEdgeRepository edgeRepository;
    private final ReviewerRecommendationRepository recommendationRepository;
    private final RecommendationEscalationRepository escalationRepository;

    public NetworkRiskPropagationServiceImpl(FraudNetworkNodeRepository nodeRepository,
                                             FraudNetworkEdgeRepository edgeRepository,
                                             ReviewerRecommendationRepository recommendationRepository,
                                             RecommendationEscalationRepository escalationRepository) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.recommendationRepository = recommendationRepository;
        this.escalationRepository = escalationRepository;
    }

    @Override
    @Transactional
    public void propagateRisk(UUID institutionId, int baseRisk) {
        Optional<FraudNetworkNode> instNodeOpt = nodeRepository.findByEntityIdAndNodeType(institutionId, FraudNetworkNode.NodeType.INSTITUTION);
        if (instNodeOpt.isEmpty()) return;

        FraudNetworkNode startNode = instNodeOpt.get();

        Queue<NodeHop> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(new NodeHop(startNode, 0, baseRisk));
        visited.add(startNode.getId());

        while (!queue.isEmpty()) {
            NodeHop curr = queue.poll();

            if (curr.node.getNodeType() == FraudNetworkNode.NodeType.DOCUMENT) {
                Optional<ReviewerRecommendation> recOpt = recommendationRepository.findByDocumentIdAndState(
                        curr.node.getEntityId(), ReviewerRecommendation.RecommendationState.ACTIVE);

                if (recOpt.isPresent()) {
                    ReviewerRecommendation rec = recOpt.get();
                    int points = Math.max(0, curr.riskValue / (curr.hopCount + 1));

                    Optional<RecommendationEscalation> existingEscalationOpt = escalationRepository
                            .findByRecommendationIdAndSourceEntityIdAndSourceType(rec.getId(), institutionId, "TRUST_ANOMALY");

                    if (existingEscalationOpt.isPresent()) {
                        RecommendationEscalation existing = existingEscalationOpt.get();
                        existing.setEscalationPoints(points);
                        existing.setReason("Updated elevated risk propagated via issuing institution network graph.");
                        escalationRepository.save(existing);
                    } else {
                        RecommendationEscalation escalation = new RecommendationEscalation();
                        escalation.setRecommendationId(rec.getId());
                        escalation.setSourceEntityId(institutionId);
                        escalation.setSourceType("TRUST_ANOMALY");
                        escalation.setEscalationPoints(points);
                        escalation.setReason("Elevated risk propagated via issuing institution network graph.");
                        escalation.setActive(true);
                        escalation.setCreatedAt(Instant.now());
                        escalationRepository.save(escalation);
                    }
                }
            }

            if (curr.hopCount < 2) {
                List<FraudNetworkEdge> edges = edgeRepository.findBySourceNodeIdOrTargetNodeId(curr.node.getId(), curr.node.getId());
                for (FraudNetworkEdge edge : edges) {
                    UUID neighborId = edge.getSourceNodeId().equals(curr.node.getId()) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                    if (!visited.contains(neighborId)) {
                        Optional<FraudNetworkNode> targetNodeOpt = nodeRepository.findById(neighborId);
                        if (targetNodeOpt.isPresent()) {
                            visited.add(neighborId);
                            int decay = Math.max(1, (int)Math.round(10.0 / edge.getWeight()));
                            int nextRisk = Math.max(0, curr.riskValue - decay);
                            queue.add(new NodeHop(targetNodeOpt.get(), curr.hopCount + 1, nextRisk));
                        }
                    }
                }
            }
        }
    }

    private static class NodeHop {
        final FraudNetworkNode node;
        final int hopCount;
        final int riskValue;

        NodeHop(FraudNetworkNode node, int hopCount, int riskValue) {
            this.node = node;
            this.hopCount = hopCount;
            this.riskValue = riskValue;
        }
    }
}
