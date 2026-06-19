package com.edu.repositories;

import com.edu.entity.CalibrationProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalibrationProposalRepository extends JpaRepository<CalibrationProposal, UUID> {
    
    Optional<CalibrationProposal> findByTargetParameterAndStatus(String targetParameter, CalibrationProposal.ProposalStatus status);
    
    List<CalibrationProposal> findByStatus(CalibrationProposal.ProposalStatus status);
    
    Optional<CalibrationProposal> findFirstByTargetParameterAndStatusOrderByCreatedAtDesc(String targetParameter, CalibrationProposal.ProposalStatus status);
}
