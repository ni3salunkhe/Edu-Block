package com.edu.repositories;

import com.edu.entity.FraudCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudCampaignRepository extends JpaRepository<FraudCampaign, UUID> {
    List<FraudCampaign> findByStatus(FraudCampaign.CampaignStatus status);
}
