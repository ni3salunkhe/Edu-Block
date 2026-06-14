package com.edu.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.entity.InstitutionDocumentIssueAudit;

@Repository
public interface InstitutionDocumentIssueAuditRepository extends JpaRepository<InstitutionDocumentIssueAudit, UUID> {
    List<InstitutionDocumentIssueAudit> findByInstitutionId(UUID institutionId);
}
