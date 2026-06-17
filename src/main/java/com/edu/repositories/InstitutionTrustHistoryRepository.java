package com.edu.repositories;

import com.edu.entity.InstitutionTrustHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstitutionTrustHistoryRepository extends JpaRepository<InstitutionTrustHistory, UUID> {
    List<InstitutionTrustHistory> findByInstitutionIdOrderByTimestampDesc(UUID institutionId);
}
