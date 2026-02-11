package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.IndividualProfile;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, UUID> {}