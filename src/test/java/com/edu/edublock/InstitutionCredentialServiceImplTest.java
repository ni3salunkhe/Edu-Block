package com.edu.edublock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import com.edu.entity.AssociationStatus;
import com.edu.entity.Document;
import com.edu.entity.DocumentSource;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.entity.InstitutionDocumentIssueAudit;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.dto.InstitutionIssuedDocumentDTO;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.InstitutionAssociationRepository;
import com.edu.repositories.InstitutionDocumentIssueAuditRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.services.BlockchainService;
import com.edu.services.StorageService;
import com.edu.servicesImpl.InstitutionCredentialServiceImpl;

class InstitutionCredentialServiceImplTest {

    @Mock
    private InstitutionUserRepository institutionUserRepo;

    @Mock
    private InstitutionAssociationRepository associationRepo;

    @Mock
    private DocumentRepository documentRepo;

    @Mock
    private InstitutionDocumentIssueAuditRepository issueAuditRepo;

    @Mock
    private StorageService storageService;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private AuditService auditService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InstitutionCredentialServiceImpl credentialService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIssueDocument_Success() throws IOException {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID associationId = UUID.randomUUID();
        String docType = "Degree";
        String credRef = "REF-2026-001";

        User student = new User();
        student.setId(studentId);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setId(associationId);
        association.setStudent(student);
        association.setInstitution(institution);
        association.setStatus(AssociationStatus.APPROVED);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("Test file content bytes".getBytes());

        when(institutionUserRepo.findByUserId(adminId)).thenReturn(Optional.of(mapping));
        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(documentRepo.existsBySha256Hash(anyString())).thenReturn(false);
        when(documentRepo.existsByIssuingInstitutionIdAndCredentialReferenceAndSource(
                eq(institutionId), eq(credRef), eq(DocumentSource.INSTITUTION_ISSUED))).thenReturn(false);
        when(storageService.upload(file)).thenReturn("https://cloudinary.com/doc.pdf");
        when(documentRepo.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        // Act
        InstitutionIssuedDocumentDTO result = credentialService.issueDocument(adminId, studentId, associationId, docType, credRef, file);

        // Assert
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(credRef, result.getCredentialReference());

        verify(blockchainService).anchor(any(Document.class));
        verify(issueAuditRepo).save(any(InstitutionDocumentIssueAudit.class));
        verify(auditService).logAdminAction(
            eq(adminId),
            eq("DOCUMENT_ISSUED_BY_INSTITUTION"),
            eq("DOCUMENT"),
            any(),
            contains(credRef),
            anyString()
        );
    }

    @Test
    void testIssueDocument_DuplicateHash() throws IOException {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID associationId = UUID.randomUUID();

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setStudent(new User());
        association.getStudent().setId(studentId);
        association.setInstitution(institution);
        association.setStatus(AssociationStatus.APPROVED);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("Duplicate content".getBytes());

        when(institutionUserRepo.findByUserId(adminId)).thenReturn(Optional.of(mapping));
        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(documentRepo.existsBySha256Hash(anyString())).thenReturn(true); // Duplicate file hash

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            credentialService.issueDocument(adminId, studentId, associationId, "Degree", "REF123", file);
        });

        assertEquals("Exact duplicate document exists", exception.getMessage());
        verify(documentRepo, never()).save(any(Document.class));
        verify(auditService).logAdminAction(
            eq(adminId),
            eq("DOCUMENT_ISSUANCE_REJECTED"),
            eq("DOCUMENT"),
            any(),
            contains("Duplicate file hash"),
            anyString()
        );
    }

    @Test
    void testIssueDocument_DuplicateReference() throws IOException {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID associationId = UUID.randomUUID();
        String credRef = "REF123";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setStudent(new User());
        association.getStudent().setId(studentId);
        association.setInstitution(institution);
        association.setStatus(AssociationStatus.APPROVED);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("Unique content".getBytes());

        when(institutionUserRepo.findByUserId(adminId)).thenReturn(Optional.of(mapping));
        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(documentRepo.existsBySha256Hash(anyString())).thenReturn(false);
        when(documentRepo.existsByIssuingInstitutionIdAndCredentialReferenceAndSource(
                eq(institutionId), eq(credRef), eq(DocumentSource.INSTITUTION_ISSUED))).thenReturn(true); // Duplicate ref

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            credentialService.issueDocument(adminId, studentId, associationId, "Degree", credRef, file);
        });

        assertEquals("Duplicate credential reference already exists for this institution", exception.getMessage());
        verify(documentRepo, never()).save(any(Document.class));
        verify(auditService).logAdminAction(
            eq(adminId),
            eq("DOCUMENT_ISSUANCE_REJECTED"),
            eq("DOCUMENT"),
            any(),
            contains("Duplicate credential reference"),
            anyString()
        );
    }

    @Test
    void testIssueDocument_BlockchainFailureRollback() throws IOException {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID associationId = UUID.randomUUID();
        String credRef = "REF123";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);
        institution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        InstitutionAssociation association = new InstitutionAssociation();
        association.setStudent(new User());
        association.getStudent().setId(studentId);
        association.setInstitution(institution);
        association.setStatus(AssociationStatus.APPROVED);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("Test file content bytes".getBytes());

        when(institutionUserRepo.findByUserId(adminId)).thenReturn(Optional.of(mapping));
        when(associationRepo.findById(associationId)).thenReturn(Optional.of(association));
        when(documentRepo.existsBySha256Hash(anyString())).thenReturn(false);
        when(documentRepo.existsByIssuingInstitutionIdAndCredentialReferenceAndSource(
                eq(institutionId), eq(credRef), eq(DocumentSource.INSTITUTION_ISSUED))).thenReturn(false);
        when(documentRepo.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Blockchain anchor failure")).when(blockchainService).anchor(any(Document.class));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            credentialService.issueDocument(adminId, studentId, associationId, "Degree", credRef, file);
        });

        assertEquals("Blockchain anchor failure", exception.getMessage());
        verify(issueAuditRepo, never()).save(any(InstitutionDocumentIssueAudit.class));
    }

    @Test
    void testGetIssuedDocuments() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Institution institution = new Institution();
        institution.setId(institutionId);

        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        Document doc = new Document();
        doc.setIssuingInstitutionId(institutionId);
        doc.setOwnerId(ownerId);
        doc.setCreatedAt(Instant.now());

        User owner = new User();
        owner.setId(ownerId);
        owner.setUsername("TestStudent");

        when(institutionUserRepo.findByUserId(adminId)).thenReturn(Optional.of(mapping));
        when(documentRepo.findByIssuingInstitutionId(institutionId)).thenReturn(Collections.singletonList(doc));
        when(userRepository.findAllById(any())).thenReturn(Collections.singletonList(owner));

        // Act
        List<InstitutionIssuedDocumentDTO> result = credentialService.getIssuedDocuments(adminId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getStudentId());
        assertEquals("TestStudent", result.get(0).getStudentName());
    }
}
