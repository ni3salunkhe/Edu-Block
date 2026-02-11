package com.edu.dto;

import java.util.UUID;

import com.edu.entity.EmployerConsent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerConsentListDTO {
    private UUID id;
    private String individualName;
    private EmployerConsent.Status status;
}
