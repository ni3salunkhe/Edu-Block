package com.edu.services;

import com.edu.entity.FraudCampaign;
import com.edu.entity.FraudSignature;
import java.util.Optional;

public interface FraudSignatureMatcher {
    Optional<FraudSignature> classify(FraudCampaign campaign);
}
