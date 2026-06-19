package com.edu.repositories;

import com.edu.entity.InstitutionTrustProfile;
import com.edu.repositories.projections.InstitutionMetricsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface InstitutionTrustProfileRepository extends JpaRepository<InstitutionTrustProfile, UUID> {

    @Query("SELECT p FROM InstitutionTrustProfile p JOIN FETCH p.institution")
    java.util.List<InstitutionTrustProfile> findAllWithInstitution();

    @Query("""
        SELECT 
            COUNT(d.id) as totalIssued,
            COUNT(CASE WHEN dvr.isVerified = true THEN 1 END) as totalVerified,
            COALESCE(AVG(dvr.confidenceScore), 0.0) as avgConfidence,
            COUNT(fs.id) as totalSignals
        FROM Document d
        LEFT JOIN DocumentVerificationResult dvr ON dvr.documentId = d.id
        LEFT JOIN FraudSignal fs ON fs.document.id = d.id
        WHERE d.issuingInstitutionId = :institutionId
    """)
    InstitutionMetricsProjection aggregateInstitutionMetrics(@Param("institutionId") UUID institutionId);
}
