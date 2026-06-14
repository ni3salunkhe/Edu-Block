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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.edu.dto.CreateInstitutionRequest;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionVerificationAudit;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionVerificationAuditRepository;
import com.edu.services.AuditService;
import com.edu.servicesImpl.InstitutionServiceImpl;

class InstitutionServiceImplTest {

    @Mock
    private InstitutionRepository institutionRepo;

    @Mock
    private InstitutionVerificationAuditRepository verificationAuditRepo;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInstitution_Success() {
        // Arrange
        CreateInstitutionRequest request = new CreateInstitutionRequest(
            "Test University",
            "  tU123  ", // Needs trimming & uppercasing -> "TU123"
            "https://test.edu",
            "info@test.edu",
            "City",
            "State",
            "Country"
        );

        when(institutionRepo.existsByCode("TU123")).thenReturn(false);
        when(institutionRepo.existsByOfficialEmail("info@test.edu")).thenReturn(false);

        Institution savedMock = new Institution();
        savedMock.setId(UUID.randomUUID());
        savedMock.setName("Test University");
        savedMock.setCode("TU123");
        savedMock.setWebsite("https://test.edu");
        savedMock.setOfficialEmail("info@test.edu");
        savedMock.setCity("City");
        savedMock.setState("State");
        savedMock.setCountry("Country");
        savedMock.setTrustLevel(InstitutionTrustLevel.REGISTERED);
        savedMock.setStatus(InstitutionStatus.ACTIVE);

        when(institutionRepo.save(any(Institution.class))).thenReturn(savedMock);

        // Act
        Institution result = institutionService.createInstitution(request);

        // Assert
        assertNotNull(result);
        assertEquals("TU123", result.getCode());
        assertEquals(InstitutionTrustLevel.REGISTERED, result.getTrustLevel());
        assertEquals(InstitutionStatus.ACTIVE, result.getStatus());

        // Verify Repository interactions
        verify(institutionRepo).existsByCode("TU123");
        verify(institutionRepo).existsByOfficialEmail("info@test.edu");
        
        ArgumentCaptor<Institution> entityCaptor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionRepo).save(entityCaptor.capture());
        
        Institution captured = entityCaptor.getValue();
        assertEquals("TU123", captured.getCode());
        assertEquals(InstitutionTrustLevel.REGISTERED, captured.getTrustLevel());
        assertEquals(InstitutionStatus.ACTIVE, captured.getStatus());

        // Verify Audit interaction
        verify(auditService).logAdminAction(
            any(UUID.class),
            eq("CREATE_INSTITUTION"),
            eq("INSTITUTION"),
            eq(savedMock.getId()),
            contains("TU123"),
            anyString()
        );
    }

    @Test
    void testCreateInstitution_DuplicateCode() {
        // Arrange
        CreateInstitutionRequest request = new CreateInstitutionRequest(
            "Test University",
            "TU123",
            "https://test.edu",
            "info@test.edu",
            "City",
            "State",
            "Country"
        );

        when(institutionRepo.existsByCode("TU123")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.createInstitution(request);
        });

        assertEquals("Institution code already exists", exception.getMessage());
        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testCreateInstitution_DuplicateEmail() {
        // Arrange
        CreateInstitutionRequest request = new CreateInstitutionRequest(
            "Test University",
            "TU123",
            "https://test.edu",
            "info@test.edu",
            "City",
            "State",
            "Country"
        );

        when(institutionRepo.existsByCode("TU123")).thenReturn(false);
        when(institutionRepo.existsByOfficialEmail("info@test.edu")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.createInstitution(request);
        });

        assertEquals("Institution email already exists", exception.getMessage());
        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testGetInstitution_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Institution mockInst = new Institution();
        mockInst.setId(id);
        mockInst.setName("Test Uni");

        when(institutionRepo.findById(id)).thenReturn(Optional.of(mockInst));

        // Act
        Institution result = institutionService.getInstitution(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Uni", result.getName());
    }

    @Test
    void testGetInstitution_NotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(institutionRepo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            institutionService.getInstitution(id);
        });
    }

    @Test
    void testListInstitutions() {
        // Arrange
        Institution mockInst = new Institution();
        mockInst.setName("Test Uni");
        when(institutionRepo.findAll()).thenReturn(Collections.singletonList(mockInst));

        // Act
        List<Institution> result = institutionService.listInstitutions();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Uni", result.get(0).getName());
    }

    @Test
    void testVerifyInstitution_Success() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Verification reason minimum ten chars";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test Uni");
        institution.setTrustLevel(InstitutionTrustLevel.REGISTERED);
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionRepo.save(any(Institution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Institution result = institutionService.verifyInstitution(institutionId, reason, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(InstitutionTrustLevel.VERIFIED, result.getTrustLevel());

        verify(institutionRepo).save(institution);
        verify(verificationAuditRepo).save(any(InstitutionVerificationAudit.class));
        verify(auditService).logAdminAction(
            eq(adminId),
            eq("VERIFY_INSTITUTION"),
            eq("INSTITUTION"),
            eq(institutionId),
            contains("REGISTERED"),
            anyString()
        );
    }

    @Test
    void testVerifyInstitution_InvalidReason() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        // Act & Assert for null/blank reason
        assertThrows(IllegalArgumentException.class, () -> {
            institutionService.verifyInstitution(institutionId, "   ", adminId);
        });

        // Act & Assert for too short reason
        assertThrows(IllegalArgumentException.class, () -> {
            institutionService.verifyInstitution(institutionId, "Short", adminId);
        });

        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testVerifyInstitution_NullAdmin() {
        // Arrange
        UUID institutionId = UUID.randomUUID();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            institutionService.verifyInstitution(institutionId, "Valid reason text here", null);
        });

        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testVerifyInstitution_InactiveStatus() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Verification reason minimum ten chars";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test Uni");
        institution.setTrustLevel(InstitutionTrustLevel.REGISTERED);
        institution.setStatus(InstitutionStatus.SUSPENDED);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.verifyInstitution(institutionId, reason, adminId);
        });

        assertEquals("Institution must be ACTIVE for trust level transition", exception.getMessage());
        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testVerifyInstitution_InvalidTransition() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Verification reason minimum ten chars";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test Uni");
        institution.setTrustLevel(InstitutionTrustLevel.VERIFIED); // Already VERIFIED
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.verifyInstitution(institutionId, reason, adminId);
        });

        assertEquals("Invalid transition: current trust level must be REGISTERED to verify", exception.getMessage());
        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testAccreditInstitution_Success() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Accreditation reason minimum ten chars";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test Uni");
        institution.setTrustLevel(InstitutionTrustLevel.VERIFIED); // Must be VERIFIED first
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionRepo.save(any(Institution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Institution result = institutionService.accreditInstitution(institutionId, reason, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(InstitutionTrustLevel.ACCREDITED, result.getTrustLevel());

        verify(institutionRepo).save(institution);
        verify(verificationAuditRepo).save(any(InstitutionVerificationAudit.class));
        verify(auditService).logAdminAction(
            eq(adminId),
            eq("ACCREDIT_INSTITUTION"),
            eq("INSTITUTION"),
            eq(institutionId),
            contains("VERIFIED"),
            anyString()
        );
    }

    @Test
    void testAccreditInstitution_InvalidTransition() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Accreditation reason minimum ten chars";

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test Uni");
        institution.setTrustLevel(InstitutionTrustLevel.REGISTERED); // Cannot transition REGISTERED -> ACCREDITED directly
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionService.accreditInstitution(institutionId, reason, adminId);
        });

        assertEquals("Invalid transition: current trust level must be VERIFIED to accredit", exception.getMessage());
        verify(institutionRepo, never()).save(any(Institution.class));
    }

    @Test
    void testGetAuditHistory() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        Institution institution = new Institution();
        institution.setId(institutionId);

        InstitutionVerificationAudit audit = new InstitutionVerificationAudit();
        audit.setReason("Test reason minimum ten chars");

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(verificationAuditRepo.findByInstitutionOrderByChangedAtDesc(institution))
            .thenReturn(Collections.singletonList(audit));

        // Act
        List<InstitutionVerificationAudit> history = institutionService.getAuditHistory(institutionId);

        // Assert
        assertEquals(1, history.size());
        assertEquals("Test reason minimum ten chars", history.get(0).getReason());
    }
}
