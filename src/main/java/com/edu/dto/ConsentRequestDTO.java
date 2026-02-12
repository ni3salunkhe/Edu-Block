package com.edu.dto;

import java.util.Objects;
import java.util.UUID;

public class ConsentRequestDTO {
    private UUID individualId;

    public UUID getIndividualId() {
        return individualId;
    }

    public void setIndividualId(UUID individualId) {
        this.individualId = individualId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentRequestDTO that = (ConsentRequestDTO) o;
        return Objects.equals(individualId, that.individualId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(individualId);
    }

    @Override
    public String toString() {
        return "ConsentRequestDTO{" +
                "individualId=" + individualId +
                '}';
    }
}
