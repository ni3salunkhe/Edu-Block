package com.edu.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.entity.Institution;
import com.edu.entity.InstitutionVerificationAudit;

@Repository
public interface InstitutionVerificationAuditRepository extends JpaRepository<InstitutionVerificationAudit, UUID> {
    List<InstitutionVerificationAudit> findByInstitution(Institution institution);
    List<InstitutionVerificationAudit> findByInstitutionOrderByChangedAtDesc(Institution institution);
}
