package com.edu.repositories;

import com.edu.entity.FraudSignal;
import com.edu.entity.FraudSignal.FraudSignalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudSignalRepository extends JpaRepository<FraudSignal, UUID> {
    
    List<FraudSignal> findByDocumentId(UUID documentId);
    
    @Query("SELECT fs.signalType, COUNT(fs) FROM FraudSignal fs GROUP BY fs.signalType")
    List<Object[]> getFraudSignalCounts();
    
    @Query("SELECT COUNT(fs) FROM FraudSignal fs WHERE fs.document.id = :documentId AND fs.signalType = :signalType")
    long countByDocumentIdAndSignalType(UUID documentId, FraudSignalType signalType);

    @Query("SELECT COUNT(fs) FROM FraudSignal fs WHERE fs.document.id != :documentId AND fs.document.ownerId != :ownerId AND fs.document.credentialReference = :credentialReference")
    long countReferenceReuseExcludingOwner(UUID documentId, UUID ownerId, String credentialReference);
}
