package com.edu.util;

import com.edu.dto.TrustScoreBreakdownDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TrustScoreBreakdownConverter implements AttributeConverter<TrustScoreBreakdownDTO, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrustScoreBreakdownConverter() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String convertToDatabaseColumn(TrustScoreBreakdownDTO dto) {
        if (dto == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public TrustScoreBreakdownDTO convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "{}".equals(dbData)) {
            return new TrustScoreBreakdownDTO();
        }
        try {
            return objectMapper.readValue(dbData, TrustScoreBreakdownDTO.class);
        } catch (Exception e) {
            return new TrustScoreBreakdownDTO();
        }
    }
}
