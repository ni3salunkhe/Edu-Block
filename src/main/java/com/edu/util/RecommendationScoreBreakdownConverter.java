package com.edu.util;

import com.edu.dto.RecommendationScoreBreakdownDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RecommendationScoreBreakdownConverter implements AttributeConverter<RecommendationScoreBreakdownDTO, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(RecommendationScoreBreakdownDTO attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert RecommendationScoreBreakdownDTO to JSON string", e);
        }
    }

    @Override
    public RecommendationScoreBreakdownDTO convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, RecommendationScoreBreakdownDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JSON string to RecommendationScoreBreakdownDTO", e);
        }
    }
}
