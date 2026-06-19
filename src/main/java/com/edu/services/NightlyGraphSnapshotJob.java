package com.edu.services;

import com.edu.entity.*;
import com.edu.repositories.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
public class NightlyGraphSnapshotJob {

    private final FraudNetworkNodeRepository nodeRepository;
    private final FraudNetworkEdgeRepository edgeRepository;
    private final FraudClusterRepository clusterRepository;
    private final FraudClusterMemberRepository memberRepository;
    private final GraphVersionRepository graphVersionRepository;
    private final FraudNetworkService networkService;

    public NightlyGraphSnapshotJob(FraudNetworkNodeRepository nodeRepository,
                                   FraudNetworkEdgeRepository edgeRepository,
                                   FraudClusterRepository clusterRepository,
                                   FraudClusterMemberRepository memberRepository,
                                   GraphVersionRepository graphVersionRepository,
                                   FraudNetworkService networkService) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.clusterRepository = clusterRepository;
        this.memberRepository = memberRepository;
        this.graphVersionRepository = graphVersionRepository;
        this.networkService = networkService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void executeSnapshot() {
        networkService.executeConnectedComponentClustering();

        Instant thirtyDaysAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        List<FraudCluster> inactiveClusters = clusterRepository.findByStatusAndLastSeenAtBefore(
                FraudCluster.ClusterStatus.ACTIVE, thirtyDaysAgo);
        for (FraudCluster cluster : inactiveClusters) {
            cluster.setStatus(FraudCluster.ClusterStatus.RESOLVED);
            clusterRepository.save(cluster);
        }

        long nodeCount = nodeRepository.count();
        long edgeCount = edgeRepository.count();
        long clusterCount = clusterRepository.count();

        List<FraudNetworkNode> allNodes = nodeRepository.findAll();
        allNodes.sort(Comparator.comparing(n -> n.getId().toString()));
        StringBuilder stateBuilder = new StringBuilder();
        for (FraudNetworkNode node : allNodes) {
            stateBuilder.append(node.getId().toString()).append(":").append(node.getRiskScore()).append(";");
        }

        List<FraudNetworkEdge> allEdges = edgeRepository.findAll();
        allEdges.sort(Comparator.comparing((FraudNetworkEdge e) -> e.getSourceNodeId().toString())
                .thenComparing(e -> e.getTargetNodeId().toString()));
        for (FraudNetworkEdge edge : allEdges) {
            stateBuilder.append(edge.getSourceNodeId().toString()).append("->")
                    .append(edge.getTargetNodeId().toString()).append(":").append(edge.getRelationshipType()).append(";");
        }

        List<FraudCluster> allClusters = clusterRepository.findAll();
        allClusters.sort(Comparator.comparing(c -> c.getId().toString()));
        for (FraudCluster cluster : allClusters) {
            stateBuilder.append("cluster:").append(cluster.getId().toString())
                    .append(":").append(cluster.getStatus())
                    .append(":").append(cluster.getCommunityFingerprint())
                    .append(":").append(cluster.getScore())
                    .append(";");
        }

        List<FraudClusterMember> allMembers = memberRepository.findAll();
        allMembers.sort(Comparator.comparing((FraudClusterMember m) -> m.getClusterId().toString())
                .thenComparing(m -> m.getNodeId().toString()));
        for (FraudClusterMember member : allMembers) {
            stateBuilder.append("member:").append(member.getClusterId().toString())
                    .append("->").append(member.getNodeId().toString())
                    .append(":").append(member.getMembershipScore())
                    .append(";");
        }

        String stateHash = calculateSHA256(stateBuilder.toString());

        long nextVersion = graphVersionRepository.findFirstByOrderByVersionNumberDesc()
                .map(gv -> gv.getVersionNumber() + 1)
                .orElse(1L);

        GraphVersion snapshot = new GraphVersion();
        snapshot.setVersionNumber(nextVersion);
        snapshot.setNodeCount(nodeCount);
        snapshot.setEdgeCount(edgeCount);
        snapshot.setClusterCount(clusterCount);
        snapshot.setStateHash(stateHash);
        snapshot.setCreatedAt(Instant.now());
        graphVersionRepository.save(snapshot);
    }

    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
