package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.InstitutionProfile;

public interface InstitutionProfileRepository extends JpaRepository<InstitutionProfile, UUID> {}