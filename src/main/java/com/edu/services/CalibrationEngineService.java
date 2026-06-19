package com.edu.services;

import com.edu.entity.CalibrationProposal;
import java.util.List;
import java.util.UUID;

public interface CalibrationEngineService {
    List<CalibrationProposal> evaluateCalibration();
    void applyProposal(UUID proposalId, UUID adminId);
    void rejectProposal(UUID proposalId, UUID adminId, String reason);
}
