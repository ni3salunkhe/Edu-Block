package com.edu.repositories;

import com.edu.entity.VerificationRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRuleConfigRepository extends JpaRepository<VerificationRuleConfig, String> {
}
