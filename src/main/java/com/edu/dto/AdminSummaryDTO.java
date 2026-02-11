package com.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminSummaryDTO {
    private long totalUsers;
    private long verifiers;
    private long activeConsents;
}
