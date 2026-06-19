package com.edu.dto;

import java.util.List;
import java.util.ArrayList;

public class FraudNetworkGraphDTO {
    private List<Object> nodes = new ArrayList<>();
    private List<Object> edges = new ArrayList<>();

    public FraudNetworkGraphDTO() {}

    public List<Object> getNodes() {
        return nodes;
    }

    public void setNodes(List<Object> nodes) {
        this.nodes = nodes;
    }

    public List<Object> getEdges() {
        return edges;
    }

    public void setEdges(List<Object> edges) {
        this.edges = edges;
    }
}
