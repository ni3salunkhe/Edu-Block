package com.edu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.edu.entity.Institution;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
    Optional<Institution> findByCode(String code);
    Optional<Institution> findByOfficialEmail(String officialEmail);
    List<Institution> findByName(String name);
    boolean existsByCode(String code);
    boolean existsByOfficialEmail(String officialEmail);
    
    Optional<Institution> findByNameIgnoreCase(String name);
    Optional<Institution> findByNormalizedName(String normalizedName);

    @Query("SELECT i FROM Institution i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :token, '%'))")
    Optional<Institution> findByNameContainingIgnoreCase(@Param("token") String token);
}
