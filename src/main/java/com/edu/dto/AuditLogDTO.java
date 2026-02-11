package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
    private UUID id;
    private Instant timestamp;
    private String actor;
    private String action;
    private String target;
}
