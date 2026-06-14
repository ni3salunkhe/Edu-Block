package com.edu.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.EmployerConsent;

public interface EmployerConsentRepository extends JpaRepository<EmployerConsent, UUID> {

	Optional<EmployerConsent> findByEmployerIdAndOwnerId(
			UUID employerId, UUID ownerId
		);
	List<EmployerConsent> findByOwnerIdAndStatus(
			UUID ownerId,
			EmployerConsent.Status status
	);

	long countByStatus(EmployerConsent.Status status);
	
	long countByOwnerIdAndStatus(UUID ownerId, EmployerConsent.Status status);
	
	long countByEmployerIdAndStatus(UUID employerId, EmployerConsent.Status status);
	
	List<EmployerConsent> findByEmployerId(UUID employerId);

	List<EmployerConsent> findByOwnerIdAndStatusIn(UUID ownerId, Collection<EmployerConsent.Status> statuses);

}
