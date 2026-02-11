package com.edu.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.AdminMfa;

public interface AdminMfaRepository extends JpaRepository<AdminMfa, UUID> {
}
