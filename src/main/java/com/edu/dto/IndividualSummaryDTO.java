package com.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndividualSummaryDTO {
    private long totalDocuments;
    private long pendingConsents;
    private long activeEmployers;
}
