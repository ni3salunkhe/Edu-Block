package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

import com.edu.entity.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private UUID id;
    private String fileName;
    private Document.Status status;
    private Instant createdAt;
    private String filePath;
}
