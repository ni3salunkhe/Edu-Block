package com.edu.repositories;

import com.edu.entity.GraphVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GraphVersionRepository extends JpaRepository<GraphVersion, UUID> {
    Optional<GraphVersion> findFirstByOrderByVersionNumberDesc();
}
