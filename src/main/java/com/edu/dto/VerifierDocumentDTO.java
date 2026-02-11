package com.edu.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifierDocumentDTO {
    private UUID id;
    private String fileName;
    private String ownerName;
    private String status;
    private String aiStatus; // e.g., "Non-Original" or "Unique" or AI score description
    private String sha256Hash;
    private String blockchainHash;
    private String previewUrl;
}
