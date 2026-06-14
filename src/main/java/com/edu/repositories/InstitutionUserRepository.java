package com.edu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.entity.Institution;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;

@Repository
public interface InstitutionUserRepository extends JpaRepository<InstitutionUser, UUID> {
    Optional<InstitutionUser> findByUser(User user);
    Optional<InstitutionUser> findByUserId(UUID userId);
    List<InstitutionUser> findByInstitution(Institution institution);
    boolean existsByUserIdAndInstitutionId(UUID userId, UUID institutionId);
    boolean existsByUserId(UUID userId);
}
