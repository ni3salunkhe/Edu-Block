package com.edu.util;

import com.edu.dto.EvidenceSnapshotDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EvidenceSnapshotConverter implements AttributeConverter<EvidenceSnapshotDTO, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(EvidenceSnapshotDTO attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert EvidenceSnapshotDTO to JSON string", e);
        }
    }

    @Override
    public EvidenceSnapshotDTO convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, EvidenceSnapshotDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JSON string to EvidenceSnapshotDTO", e);
        }
    }
}
