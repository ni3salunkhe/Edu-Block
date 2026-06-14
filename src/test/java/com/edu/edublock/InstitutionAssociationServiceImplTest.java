package com.edu.edublock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import com.edu.entity.AssociationStatus;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.repositories.InstitutionAssociationRepository;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.servicesImpl.InstitutionAssociationServiceImpl;

class InstitutionAssociationServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private InstitutionRepository institutionRepo;

    @Mock
    private InstitutionAssociationRepository associationRepo;

    @Mock
    private InstitutionUserRepository institutionUserRepo;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InstitutionAssociationServiceImpl associationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRequestAssociation_Success() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        String enrollment = "ENROLL123";

        User student = new User();
        student.setId(studentId);
        student.setRole(User.Role.INDIVIDUAL);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        when(userRepo.findById(studentId)).thenReturn(Optional.of(student));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(associationRepo.existsByStudentAndInstitutionAndStatus(student, institution, AssociationStatus.PENDING)).thenReturn(false);
        when(associationRepo.existsByStudentAndStatus(student, AssociationStatus.APPROVED)).thenReturn(false);

        InstitutionAssociation savedMock = new InstitutionAssociation();
        savedMock.setId(UUID.randomUUID());
        savedMock.setStudent(student);
        savedMock.setInstitution(institution);
        savedMock.setEnrollmentNumber(enrollment);
        savedMock.setStatus(AssociationStatus.PENDING);

        when(associationRepo.save(any(InstitutionAssociation.class))).thenReturn(savedMock);

        // Act
        InstitutionAssociation result = associationService.requestAssociation(studentId, institutionId, enrollment);

        // Assert
        assertNotNull(result);
        assertEquals(AssociationStatus.PENDING, result.getStatus());
        assertEquals(enrollment, result.getEnrollmentNumber());

        verify(associationRepo).save(any(InstitutionAssociation.class));
        verify(auditService).logIndividualAction(
            eq(studentId),
            eq("ASSOCIATION_REQUESTED"),
            eq("ASSOCIATION"),
            eq(savedMock.getId()),
            anyString(),
            anyString()
        );
    }

    @Test
    void testRequestAssociation_InvalidRole() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User student = new User();
        student.setId(studentId);
        student.setRole(User.Role.EMPLOYER); // Invalid role (must be INDIVIDUAL)

        when(userRepo.findById(studentId)).thenReturn(Optional.of(student));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            associationService.requestAssociation(studentId, institutionId, "ENROLL123");
        });

        assertEquals("Only users with role INDIVIDUAL may request association", exception.getMessage());
        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testRequestAssociation_InstitutionNotAccredited() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User student = new User();
        student.setId(studentId);
        student.setRole(User.Role.INDIVIDUAL);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.VERIFIED); // Invalid trust level (must be ACCREDITED)

        when(userRepo.findById(studentId)).thenReturn(Optional.of(student));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            associationService.requestAssociation(studentId, institutionId, "ENROLL123");
        });

        assertEquals("Institution must be ACCREDITED to request association", exception.getMessage());
        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testRequestAssociation_DuplicatePending() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User student = new User();
        student.setId(studentId);
        student.setRole(User.Role.INDIVIDUAL);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        when(userRepo.findById(studentId)).thenReturn(Optional.of(student));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(associationRepo.existsByStudentAndInstitutionAndStatus(student, institution, AssociationStatus.PENDING)).thenReturn(true); // Duplicate pending

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            associationService.requestAssociation(studentId, institutionId, "ENROLL123");
        });

        assertEquals("Duplicate pending request exists for this institution", exception.getMessage());
        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testRequestAssociation_ActiveAssociationExists() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User student = new User();
        student.setId(studentId);
        student.setRole(User.Role.INDIVIDUAL);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        when(userRepo.findById(studentId)).thenReturn(Optional.of(student));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(associationRepo.existsByStudentAndInstitutionAndStatus(student, institution, AssociationStatus.PENDING)).thenReturn(false);
        when(associationRepo.existsByStudentAndStatus(student, AssociationStatus.APPROVED)).thenReturn(true); // Active exists

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            associationService.requestAssociation(studentId, institutionId, "ENROLL123");
        });

        assertEquals("Student already has an active approved association", exception.getMessage());
        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testApproveAssociation_Success() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        User student = new User();

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStatus(AssociationStatus.PENDING);
        association.setInstitution(institution);
        association.setStudent(student);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(institutionUserRepo.findByUserId(institutionUserId)).thenReturn(Optional.of(mapping));
        when(associationRepo.existsByStudentAndStatus(student, AssociationStatus.APPROVED)).thenReturn(false);
        when(associationRepo.save(any(InstitutionAssociation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstitutionAssociation result = associationService.approveAssociation(associationId, institutionUserId);

        // Assert
        assertNotNull(result);
        assertEquals(AssociationStatus.APPROVED, result.getStatus());
        assertEquals(institutionUserId, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());

        verify(associationRepo).save(association);
        verify(auditService).logAdminAction(
            eq(institutionUserId),
            eq("ASSOCIATION_APPROVED"),
            eq("ASSOCIATION"),
            eq(associationId),
            anyString(),
            anyString()
        );
    }

    @Test
    void testApproveAssociation_AuthorizationFailure_Unmapped() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStatus(AssociationStatus.PENDING);
        association.setInstitution(new Institution());

        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(institutionUserRepo.findByUserId(institutionUserId)).thenReturn(Optional.empty()); // Unmapped

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            associationService.approveAssociation(associationId, institutionUserId);
        });

        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testApproveAssociation_AuthorizationFailure_Mismatch() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();

        Institution institutionA = new Institution();
        institutionA.setId(UUID.randomUUID());

        Institution institutionB = new Institution();
        institutionB.setId(UUID.randomUUID());

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStatus(AssociationStatus.PENDING);
        association.setInstitution(institutionA);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institutionB); // Mapped to different institution

        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(institutionUserRepo.findByUserId(institutionUserId)).thenReturn(Optional.of(mapping));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            associationService.approveAssociation(associationId, institutionUserId);
        });

        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testRejectAssociation_Success() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        String reason = "Reason for rejection is long enough";

        Institution institution = new Institution();
        institution.setId(institutionId);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStatus(AssociationStatus.PENDING);
        association.setInstitution(institution);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(institutionUserRepo.findByUserId(institutionUserId)).thenReturn(Optional.of(mapping));
        when(associationRepo.save(any(InstitutionAssociation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstitutionAssociation result = associationService.rejectAssociation(associationId, institutionUserId, reason);

        // Assert
        assertNotNull(result);
        assertEquals(AssociationStatus.REJECTED, result.getStatus());
        assertEquals(reason, result.getReviewReason());

        verify(associationRepo).save(association);
        verify(auditService).logAdminAction(
            eq(institutionUserId),
            eq("ASSOCIATION_REJECTED"),
            eq("ASSOCIATION"),
            eq(associationId),
            contains("reason"),
            anyString()
        );
    }

    @Test
    void testRejectAssociation_InvalidReason() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();

        // Act & Assert (too short)
        assertThrows(IllegalArgumentException.class, () -> {
            associationService.rejectAssociation(associationId, institutionUserId, "Short");
        });

        // Act & Assert (blank)
        assertThrows(IllegalArgumentException.class, () -> {
            associationService.rejectAssociation(associationId, institutionUserId, "          ");
        });

        verify(associationRepo, never()).save(any(InstitutionAssociation.class));
    }

    @Test
    void testRevokeAssociation_Success() {
        // Arrange
        UUID associationId = UUID.randomUUID();
        UUID institutionUserId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        String reason = "Reason for revoking is long enough";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStatus(AssociationStatus.APPROVED);
        association.setInstitution(institution);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(institutionUserRepo.findByUserId(institutionUserId)).thenReturn(Optional.of(mapping));
        when(associationRepo.save(any(InstitutionAssociation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InstitutionAssociation result = associationService.revokeAssociation(associationId, institutionUserId, reason);

        // Assert
        assertNotNull(result);
        assertEquals(AssociationStatus.REVOKED, result.getStatus());
        assertEquals(reason, result.getReviewReason());

        verify(associationRepo).save(association);
        verify(auditService).logAdminAction(
            eq(institutionUserId),
            eq("ASSOCIATION_REVOKED"),
            eq("ASSOCIATION"),
            eq(associationId),
            contains("reason"),
            anyString()
        );
    }
}
