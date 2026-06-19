package com.edu.servicesImpl;

import com.edu.entity.FraudCampaign;
import com.edu.entity.FraudSignature;
import com.edu.repositories.FraudSignatureRepository;
import com.edu.services.FraudSignatureMatcher;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FraudSignatureMatcherImpl implements FraudSignatureMatcher {

    private final FraudSignatureRepository signatureRepository;

    public FraudSignatureMatcherImpl(FraudSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public Optional<FraudSignature> classify(FraudCampaign campaign) {
        List<FraudSignature> signatures = signatureRepository.findAll();
        for (FraudSignature sig : signatures) {
            if (isMatchingSignature(campaign, sig)) {
                sig.setHistoricalCampaignsCount(sig.getHistoricalCampaignsCount() + 1);
                signatureRepository.save(sig);
                return Optional.of(sig);
            }
        }
        return Optional.empty();
    }

    private boolean isMatchingSignature(FraudCampaign campaign, FraudSignature sig) {
        String sigName = sig.getName();
        if ("Credential Reference Farming".equalsIgnoreCase(sigName)) {
            return "CREDENTIAL_REF".equalsIgnoreCase(campaign.getSignalType()) && campaign.getTotalCount() >= 5;
        } else if ("Template Clone Ring".equalsIgnoreCase(sigName)) {
            return campaign.getTemplateIdentifier() != null && campaign.getTotalCount() >= 10;
        }
        return false;
    }
}
