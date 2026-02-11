package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
