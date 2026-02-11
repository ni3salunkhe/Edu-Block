package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.VerifierProfile;

public interface VerifierProfileRepository extends JpaRepository<VerifierProfile, UUID> {}