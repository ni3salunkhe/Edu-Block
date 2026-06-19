package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.CalibrationEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CalibrationEngineServiceImpl implements CalibrationEngineService {

    private final RulePerformanceMetricRepository metricRepository;
    private final CalibrationProposalRepository proposalRepository;
    private final VerificationRuleConfigRepository ruleConfigRepository;

    public CalibrationEngineServiceImpl(RulePerformanceMetricRepository metricRepository,
                                         CalibrationProposalRepository proposalRepository,
                                         VerificationRuleConfigRepository ruleConfigRepository) {
        this.metricRepository = metricRepository;
        this.proposalRepository = proposalRepository;
        this.ruleConfigRepository = ruleConfigRepository;
    }

    @Override
    @Transactional
    public List<CalibrationProposal> evaluateCalibration() {
        List<RulePerformanceMetric> latestMetrics = metricRepository.findLatestMetrics();
        List<CalibrationProposal> proposals = new ArrayList<>();

        for (RulePerformanceMetric m : latestMetrics) {
            if (!"RULE".equals(m.getTargetType())) {
                continue;
            }

            // Guard 1: Minimum 50 observations required
            if (m.getTotalEvaluations() < 50) {
                continue;
            }

            // Check if there was any approved calibration for this target parameter recently to enforce 30 days cooldown
            // We search for the last proposal that was approved
            Optional<CalibrationProposal> lastApprovedOpt = proposalRepository
                    .findFirstByTargetParameterAndStatusOrderByCreatedAtDesc(m.getRuleName(), CalibrationProposal.ProposalStatus.APPROVED);
            
            if (lastApprovedOpt.isPresent()) {
                Instant lastCalib = lastApprovedOpt.get().getDecisionAt();
                if (lastCalib != null && lastCalib.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                    // Guard 2: Cooldown period of 30 days active, skip proposal generation
                    continue;
                }
            }

            // Calibration Rule: Trigger proposal if override rate is higher than 15%
            if (m.getOverrideRate() > 0.15) {
                Optional<VerificationRuleConfig> configOpt = ruleConfigRepository.findById(m.getRuleName());
                if (configOpt.isPresent()) {
                    VerificationRuleConfig rule = configOpt.get();
                    int currentVal = rule.getScoreModifier();

                    // Guard 3: Maximum modifier shift: 30%
                    int maxShift = (int) Math.round(Math.abs(currentVal) * 0.3);
                    if (maxShift == 0) {
                        maxShift = 1;
                    }
                    
                    // Propose a reduction in penalty or boost based on high override rate
                    // If currentVal is negative (e.g. -25), we want to make it less negative (e.g. -18)
                    // If currentVal is positive (e.g. +20), we adjust downwards if verifiers overridden to reject
                    int proposedVal;
                    if (currentVal < 0) {
                        proposedVal = currentVal + maxShift; // E.g., -25 -> -18 (shift of +7)
                        if (proposedVal > 0) proposedVal = 0;
                    } else {
                        proposedVal = currentVal - maxShift; // E.g., 20 -> 14
                        if (proposedVal < 0) proposedVal = 0;
                    }

                    // Proposal Confidence calculation
                    String confidence = "MEDIUM";
                    if (m.getTotalEvaluations() >= 150) {
                        confidence = "HIGH";
                    } else if (m.getTotalEvaluations() < 75) {
                        confidence = "LOW";
                    }

                    // Query existing unresolved/pending proposal to supersede it
                    Optional<CalibrationProposal> existingPendingOpt = proposalRepository
                            .findByTargetParameterAndStatus(m.getRuleName(), CalibrationProposal.ProposalStatus.PENDING);

                    int calibrationCount = lastApprovedOpt.map(p -> p.getCalibrationCount() + 1).orElse(1);

                    CalibrationProposal proposal = new CalibrationProposal();
                    proposal.setTargetParameter(m.getRuleName());
                    proposal.setParameterType("RULE_MODIFIER");
                    proposal.setPreviousValue(String.valueOf(currentVal));
                    proposal.setProposedValue(String.valueOf(proposedVal));
                    proposal.setStatus(CalibrationProposal.ProposalStatus.PENDING);
                    proposal.setProposalConfidence(confidence);
                    proposal.setSampleSize(m.getTotalEvaluations().intValue());
                    proposal.setOverrideRate(m.getOverrideRate());
                    proposal.setPrecisionRate(m.getPrecisionRate());
                    proposal.setCalibrationCount(calibrationCount);
                    proposal.setCreatedAt(Instant.now());

                    String justification = String.format(
                            "Verification rule '%s' triggered overrides in %.1f%% of cases (%d/%d reviews). " +
                            "This suggests the modifier score of %d is overly aggressive and causes excessive recommendation overrides. " +
                            "Proposed modifier calibrated to %d (shift of %d%%, within 30%% boundary).",
                            m.getRuleName(), m.getOverrideRate() * 100, m.getOverrideCount(), m.getTotalEvaluations(),
                            currentVal, proposedVal, (int)Math.round((double)maxShift/Math.max(1, Math.abs(currentVal)) * 100.0)
                    );
                    proposal.setJustification(justification);

                    if (existingPendingOpt.isPresent()) {
                        CalibrationProposal oldProposal = existingPendingOpt.get();
                        oldProposal.setStatus(CalibrationProposal.ProposalStatus.SUPERSEDED);
                        proposalRepository.save(oldProposal);
                    }

                    proposals.add(proposalRepository.save(proposal));
                }
            }
        }
        return proposals;
    }

    @Override
    @Transactional
    public void applyProposal(UUID proposalId, UUID adminId) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != CalibrationProposal.ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not pending approval.");
        }

        if ("RULE_MODIFIER".equals(proposal.getParameterType())) {
            VerificationRuleConfig rule = ruleConfigRepository.findById(proposal.getTargetParameter())
                    .orElseThrow(() -> new IllegalArgumentException("Rule configuration not found: " + proposal.getTargetParameter()));
            
            rule.setScoreModifier(Integer.parseInt(proposal.getProposedValue()));
            ruleConfigRepository.save(rule);
        }

        proposal.setStatus(CalibrationProposal.ProposalStatus.APPROVED);
        proposal.setDecisionBy(adminId);
        proposal.setDecisionAt(Instant.now());
        proposal.setLastCalibrationAt(Instant.now());
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void rejectProposal(UUID proposalId, UUID adminId, String reason) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != CalibrationProposal.ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not pending approval.");
        }

        proposal.setStatus(CalibrationProposal.ProposalStatus.REJECTED);
        proposal.setDecisionBy(adminId);
        proposal.setDecisionAt(Instant.now());
        proposal.setRejectionReason(reason);
        proposalRepository.save(proposal);
    }
}
