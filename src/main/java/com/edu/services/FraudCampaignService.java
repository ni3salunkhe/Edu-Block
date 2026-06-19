package com.edu.services;

import com.edu.entity.FraudCampaign;
import com.edu.entity.FraudCampaign.CampaignStatus;
import java.util.UUID;

public interface FraudCampaignService {
    FraudCampaign transitionStatus(UUID campaignId, CampaignStatus newStatus);
}
