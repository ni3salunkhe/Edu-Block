package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.EmployerProfile;

public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, UUID> {}