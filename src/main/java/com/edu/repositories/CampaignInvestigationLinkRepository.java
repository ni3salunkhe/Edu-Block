package com.edu.repositories;

import com.edu.entity.CampaignInvestigationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignInvestigationLinkRepository extends JpaRepository<CampaignInvestigationLink, UUID> {
    List<CampaignInvestigationLink> findByCampaignId(UUID campaignId);
    List<CampaignInvestigationLink> findByInvestigationCaseId(UUID investigationCaseId);
}
