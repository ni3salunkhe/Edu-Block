package com.edu.servicesImpl;

import com.edu.entity.FraudCampaign;
import com.edu.entity.FraudCampaign.CampaignStatus;
import com.edu.repositories.FraudCampaignRepository;
import com.edu.services.FraudCampaignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class FraudCampaignServiceImpl implements FraudCampaignService {

    private final FraudCampaignRepository campaignRepository;

    public FraudCampaignServiceImpl(FraudCampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    @Transactional
    public FraudCampaign transitionStatus(UUID campaignId, CampaignStatus newStatus) {
        FraudCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        CampaignStatus currentStatus = campaign.getStatus();
        if (currentStatus == newStatus) {
            return campaign;
        }

        boolean valid = false;
        switch (currentStatus) {
            case NEW:
                valid = (newStatus == CampaignStatus.MONITORING);
                break;
            case MONITORING:
                valid = (newStatus == CampaignStatus.ACTIVE || newStatus == CampaignStatus.CLOSED);
                break;
            case ACTIVE:
                valid = (newStatus == CampaignStatus.CONTAINED || newStatus == CampaignStatus.CLOSED);
                break;
            case CONTAINED:
                valid = (newStatus == CampaignStatus.CLOSED || newStatus == CampaignStatus.ACTIVE);
                break;
            case CLOSED:
                valid = false;
                break;
        }

        if (!valid) {
            throw new IllegalStateException("Invalid state transition from " + currentStatus + " to " + newStatus);
        }

        campaign.setStatus(newStatus);
        campaign.setUpdatedAt(Instant.now());
        return campaignRepository.save(campaign);
    }
}
