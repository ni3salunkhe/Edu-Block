package com.edu.servicesImpl;

import com.edu.dto.FraudNetworkGraphDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.FraudNetworkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class FraudNetworkServiceImpl implements FraudNetworkService {

    private final FraudNetworkNodeRepository nodeRepository;
    private final FraudNetworkEdgeRepository edgeRepository;
    private final FraudClusterRepository clusterRepository;
    private final FraudClusterMemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;

    public FraudNetworkServiceImpl(FraudNetworkNodeRepository nodeRepository,
                                   FraudNetworkEdgeRepository edgeRepository,
                                   FraudClusterRepository clusterRepository,
                                   FraudClusterMemberRepository memberRepository,
                                   DocumentRepository documentRepository,
                                   DocumentVerificationResultRepository verificationResultRepository) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.clusterRepository = clusterRepository;
        this.memberRepository = memberRepository;
        this.documentRepository = documentRepository;
        this.verificationResultRepository = verificationResultRepository;
    }

    @Override
    @Transactional
    public void buildRelationships(UUID documentId) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return;

        FraudNetworkNode docNode = getOrCreateNode(doc.getId(), FraudNetworkNode.NodeType.DOCUMENT, 50);

        if (doc.getIssuingInstitutionId() != null) {
            FraudNetworkNode instNode = getOrCreateNode(doc.getIssuingInstitutionId(), FraudNetworkNode.NodeType.INSTITUTION, 30);
            createEdge(docNode.getId(), instNode.getId(), FraudNetworkEdge.RelationshipType.ISSUED_BY, 1.0);
        }

        if (doc.getOwnerId() != null) {
            FraudNetworkNode studentNode = getOrCreateNode(doc.getOwnerId(), FraudNetworkNode.NodeType.STUDENT, 10);
            createEdge(docNode.getId(), studentNode.getId(), FraudNetworkEdge.RelationshipType.OWNED_BY, 1.0);
        }

        if (doc.getPerceptualHash() != null) {
            List<Document> visualMatches = documentRepository.findByPerceptualHash(doc.getPerceptualHash());
            for (Document match : visualMatches) {
                if (match.getId().equals(doc.getId())) continue;

                FraudNetworkNode otherNode = getOrCreateNode(match.getId(), FraudNetworkNode.NodeType.DOCUMENT, 50);
                createEdge(docNode.getId(), otherNode.getId(), FraudNetworkEdge.RelationshipType.SHARES_VISUAL_HASH, 2.5);
            }
        }

        DocumentVerificationResult dvr = verificationResultRepository.findById(doc.getId()).orElse(null);
        if (dvr != null && dvr.getOcrTextHash() != null) {
            List<DocumentVerificationResult> ocrMatches = verificationResultRepository.findByOcrTextHash(dvr.getOcrTextHash());
            for (DocumentVerificationResult match : ocrMatches) {
                if (match.getDocumentId().equals(doc.getId())) continue;

                FraudNetworkNode otherNode = getOrCreateNode(match.getDocumentId(), FraudNetworkNode.NodeType.DOCUMENT, 50);
                createEdge(docNode.getId(), otherNode.getId(), FraudNetworkEdge.RelationshipType.SHARES_OCR_FINGERPRINT, 2.0);
            }
        }
    }

    private static final int MAX_CANDIDATE_NODES = 50000;
    private static final int MAX_CANDIDATE_EDGES = 200000;

    @Override
    @Transactional
    public List<FraudCluster> executeConnectedComponentClustering() {
        List<FraudNetworkNode> seeds = nodeRepository.findTop1000ByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(40);
        
        Set<UUID> contextNodeIds = new HashSet<>();
        for (FraudNetworkNode seed : seeds) {
            contextNodeIds.addAll(expandCandidateHops(seed.getId(), 2));
            if (contextNodeIds.size() >= MAX_CANDIDATE_NODES) {
                break;
            }
        }

        if (contextNodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<FraudNetworkNode> candidateNodes = nodeRepository.findAllById(contextNodeIds);
        
        List<FraudNetworkEdge> allCandidateEdges = edgeRepository.findBySourceNodeIdInOrTargetNodeIdIn(contextNodeIds, contextNodeIds);
        List<FraudNetworkEdge> candidateEdges = new ArrayList<>();
        for (FraudNetworkEdge edge : allCandidateEdges) {
            if (contextNodeIds.contains(edge.getSourceNodeId()) && contextNodeIds.contains(edge.getTargetNodeId())) {
                candidateEdges.add(edge);
            }
            if (candidateEdges.size() >= MAX_CANDIDATE_EDGES) {
                break;
            }
        }

        return partitionModularityCommunities(candidateNodes, candidateEdges);
    }

    private Set<UUID> expandCandidateHops(UUID startNodeId, int maxHops) {
        Set<UUID> visited = new HashSet<>();
        Queue<HopState> queue = new LinkedList<>();
        queue.add(new HopState(startNodeId, 0));
        visited.add(startNodeId);

        while (!queue.isEmpty()) {
            HopState curr = queue.poll();
            if (curr.hop < maxHops) {
                List<FraudNetworkEdge> edges = edgeRepository.findBySourceNodeIdOrTargetNodeId(curr.nodeId, curr.nodeId);
                for (FraudNetworkEdge edge : edges) {
                    UUID neighborId = edge.getSourceNodeId().equals(curr.nodeId) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                    if (!visited.contains(neighborId)) {
                        visited.add(neighborId);
                        if (visited.size() >= MAX_CANDIDATE_NODES) {
                            return visited;
                        }
                        queue.add(new HopState(neighborId, curr.hop + 1));
                    }
                }
            }
        }
        return visited;
    }

    private List<FraudCluster> partitionModularityCommunities(List<FraudNetworkNode> nodes, List<FraudNetworkEdge> edges) {
        Map<UUID, FraudNetworkNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(FraudNetworkNode::getId, java.util.function.Function.identity()));

        org.jgrapht.Graph<UUID, org.jgrapht.graph.DefaultWeightedEdge> graph = 
                new org.jgrapht.graph.SimpleWeightedGraph<>(org.jgrapht.graph.DefaultWeightedEdge.class);

        for (UUID nodeId : nodeMap.keySet()) {
            graph.addVertex(nodeId);
        }

        for (FraudNetworkEdge edge : edges) {
            if (graph.containsVertex(edge.getSourceNodeId()) && graph.containsVertex(edge.getTargetNodeId())) {
                org.jgrapht.graph.DefaultWeightedEdge e = graph.addEdge(edge.getSourceNodeId(), edge.getTargetNodeId());
                if (e != null) {
                    graph.setEdgeWeight(e, edge.getWeight());
                }
            }
        }

        org.jgrapht.alg.connectivity.ConnectivityInspector<UUID, org.jgrapht.graph.DefaultWeightedEdge> inspector = 
                new org.jgrapht.alg.connectivity.ConnectivityInspector<>(graph);
        
        List<FraudCluster> clusters = new ArrayList<>();
        int count = 1;
        for (Set<UUID> component : inspector.connectedSets()) {
            if (component.size() < 3) continue;

            List<UUID> sortedIds = new ArrayList<>(component);
            Collections.sort(sortedIds);
            String fingerprint = calculateSHA256(sortedIds.toString());

            int totalScore = 0;
            List<FraudNetworkNode> memberNodes = new ArrayList<>();
            for (UUID nid : component) {
                FraudNetworkNode node = nodeMap.get(nid);
                if (node != null) {
                    memberNodes.add(node);
                    totalScore += node.getRiskScore();
                }
            }
            int avgScore = memberNodes.isEmpty() ? 0 : totalScore / memberNodes.size();

            double totalWeight = 0;
            int componentEdgeCount = 0;
            for (org.jgrapht.graph.DefaultWeightedEdge e : graph.edgeSet()) {
                UUID source = graph.getEdgeSource(e);
                UUID target = graph.getEdgeTarget(e);
                if (component.contains(source) && component.contains(target)) {
                    totalWeight += graph.getEdgeWeight(e);
                    componentEdgeCount++;
                }
            }
            double density = memberNodes.size() <= 1 ? 0.0 : (2.0 * componentEdgeCount) / (memberNodes.size() * (memberNodes.size() - 1));
            double weightedDensity = memberNodes.isEmpty() ? 0.0 : totalWeight / memberNodes.size();

            FraudCluster cluster = clusterRepository.findByCommunityFingerprint(fingerprint)
                    .orElseGet(() -> {
                        FraudCluster newCluster = new FraudCluster();
                        newCluster.setCommunityFingerprint(fingerprint);
                        newCluster.setCreatedAt(Instant.now());
                        return newCluster;
                    });

            cluster.setName("Threat Ring Cluster " + count++);
            cluster.setScore(avgScore);
            cluster.setNodeCount(memberNodes.size());
            cluster.setEdgeCount(componentEdgeCount);
            cluster.setDensity(density);
            cluster.setWeightedDensity(weightedDensity);
            cluster.setStatus(FraudCluster.ClusterStatus.ACTIVE);
            cluster.setLastSeenAt(Instant.now());
            cluster = clusterRepository.save(cluster);

            memberRepository.deleteByClusterId(cluster.getId());
            List<FraudClusterMember> memberships = new ArrayList<>();
            for (FraudNetworkNode n : memberNodes) {
                FraudClusterMember member = new FraudClusterMember();
                member.setClusterId(cluster.getId());
                member.setNodeId(n.getId());
                member.setMembershipScore(n.getRiskScore() / 100.0);
                member.setJoinedAt(Instant.now());
                memberships.add(member);
            }
            memberRepository.saveAll(memberships);

            clusters.add(cluster);
        }
        return clusters;
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

    private static class HopState {
        final UUID nodeId;
        final int hop;
        HopState(UUID nodeId, int hop) {
            this.nodeId = nodeId;
            this.hop = hop;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FraudNetworkGraphDTO getClusterGraph(UUID clusterId) {
        return new FraudNetworkGraphDTO();
    }

    private FraudNetworkNode getOrCreateNode(UUID entityId, FraudNetworkNode.NodeType type, int baseRisk) {
        return nodeRepository.findByEntityIdAndNodeType(entityId, type)
                .orElseGet(() -> {
                    FraudNetworkNode n = new FraudNetworkNode();
                    n.setEntityId(entityId);
                    n.setNodeType(type);
                    n.setRiskScore(baseRisk);
                    n.setCreatedAt(Instant.now());
                    n.setUpdatedAt(Instant.now());
                    return nodeRepository.save(n);
                });
    }

    private void createEdge(UUID source, UUID target, FraudNetworkEdge.RelationshipType type, double weight) {
        UUID left = source.toString().compareTo(target.toString()) < 0 ? source : target;
        UUID right = source.toString().compareTo(target.toString()) < 0 ? target : source;

        if (edgeRepository.existsBySourceNodeIdAndTargetNodeIdAndRelationshipType(left, right, type)) {
            return;
        }
        FraudNetworkEdge edge = new FraudNetworkEdge();
        edge.setSourceNodeId(left);
        edge.setTargetNodeId(right);
        edge.setRelationshipType(type);
        edge.setWeight(weight);
        edge.setCreatedAt(Instant.now());
        edgeRepository.save(edge);
    }
}
