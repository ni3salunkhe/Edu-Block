package com.edu.repositories;

import com.edu.entity.FraudSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudSignatureRepository extends JpaRepository<FraudSignature, UUID> {
    Optional<FraudSignature> findByName(String name);
}
