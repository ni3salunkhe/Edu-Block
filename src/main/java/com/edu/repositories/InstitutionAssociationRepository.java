package com.edu.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.entity.AssociationStatus;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.entity.User;

@Repository
public interface InstitutionAssociationRepository extends JpaRepository<InstitutionAssociation, UUID> {
    List<InstitutionAssociation> findByStudent(User student);
    List<InstitutionAssociation> findByInstitution(Institution institution);
    List<InstitutionAssociation> findByStatus(AssociationStatus status);
    List<InstitutionAssociation> findByInstitutionAndStatus(Institution institution, AssociationStatus status);
    boolean existsByStudentAndStatus(User student, AssociationStatus status);
    boolean existsByStudentAndInstitutionAndStatus(User student, Institution institution, AssociationStatus status);
    List<InstitutionAssociation> findByStudentAndStatus(User student, AssociationStatus status);
}
