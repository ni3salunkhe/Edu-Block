package com.edu.repositories;

import com.edu.entity.InstitutionRiskEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstitutionRiskEventRepository extends JpaRepository<InstitutionRiskEvent, UUID> {
    List<InstitutionRiskEvent> findByInstitutionIdAndResolvedFalse(UUID institutionId);
    List<InstitutionRiskEvent> findByInstitutionIdOrderByTimestampDesc(UUID institutionId);
}
