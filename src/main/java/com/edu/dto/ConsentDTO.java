package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

import com.edu.entity.EmployerConsent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDTO {
    private UUID id;
    private UUID employerId;
    private String employerName;
    private EmployerConsent.Status status;
    private Instant createdAt;
}
