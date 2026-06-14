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

import com.edu.entity.Institution;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.servicesImpl.InstitutionUserServiceImpl;

class InstitutionUserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private InstitutionRepository institutionRepo;

    @Mock
    private InstitutionUserRepository institutionUserRepo;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InstitutionUserServiceImpl institutionUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMapUserToInstitution_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.INSTITUTION);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionUserRepo.existsByUserId(userId)).thenReturn(false);

        InstitutionUser expectedMapping = new InstitutionUser();
        expectedMapping.setUser(user);
        expectedMapping.setInstitution(institution);
        when(institutionUserRepo.save(any(InstitutionUser.class))).thenReturn(expectedMapping);

        // Act
        InstitutionUser result = institutionUserService.mapUserToInstitution(userId, institutionId);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(institution, result.getInstitution());

        verify(institutionUserRepo).save(any(InstitutionUser.class));
        verify(auditService).logAdminAction(
            any(UUID.class),
            eq("MAP_INSTITUTION_USER"),
            eq("INSTITUTION_USER"),
            any(),
            anyString(),
            anyString()
        );
    }

    @Test
    void testMapUserToInstitution_InvalidRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.INDIVIDUAL); // Invalid role (must be INSTITUTION)

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionUserService.mapUserToInstitution(userId, institutionId);
        });

        assertEquals("User role must be INSTITUTION", exception.getMessage());
        verify(institutionUserRepo, never()).save(any(InstitutionUser.class));
    }

    @Test
    void testMapUserToInstitution_InactiveInstitution() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.INSTITUTION);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.SUSPENDED); // Inactive (must be ACTIVE)

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionUserService.mapUserToInstitution(userId, institutionId);
        });

        assertEquals("Institution must be ACTIVE", exception.getMessage());
        verify(institutionUserRepo, never()).save(any(InstitutionUser.class));
    }

    @Test
    void testMapUserToInstitution_DuplicateMapping() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.INSTITUTION);

        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setStatus(InstitutionStatus.ACTIVE);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionUserRepo.existsByUserId(userId)).thenReturn(true); // Already mapped

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            institutionUserService.mapUserToInstitution(userId, institutionId);
        });

        assertEquals("User is already mapped to an institution", exception.getMessage());
        verify(institutionUserRepo, never()).save(any(InstitutionUser.class));
    }

    @Test
    void testRemoveUserFromInstitution_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        InstitutionUser mapping = new InstitutionUser();
        Institution institution = new Institution();
        institution.setId(UUID.randomUUID());
        mapping.setInstitution(institution);

        when(institutionUserRepo.findByUserId(userId)).thenReturn(Optional.of(mapping));

        // Act
        institutionUserService.removeUserFromInstitution(userId);

        // Assert
        verify(institutionUserRepo).delete(mapping);
        verify(auditService).logAdminAction(
            any(UUID.class),
            eq("UNMAP_INSTITUTION_USER"),
            eq("INSTITUTION_USER"),
            eq(userId),
            anyString(),
            anyString()
        );
    }

    @Test
    void testRemoveUserFromInstitution_NotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(institutionUserRepo.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            institutionUserService.removeUserFromInstitution(userId);
        });
    }

    @Test
    void testGetInstitutionForUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Institution institution = new Institution();
        InstitutionUser mapping = new InstitutionUser();
        mapping.setInstitution(institution);

        when(institutionUserRepo.findByUserId(userId)).thenReturn(Optional.of(mapping));

        // Act
        Optional<Institution> result = institutionUserService.getInstitutionForUser(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(institution, result.get());
    }

    @Test
    void testGetUsersForInstitution() {
        // Arrange
        UUID institutionId = UUID.randomUUID();
        Institution institution = new Institution();
        institution.setId(institutionId);

        User user = new User();
        InstitutionUser mapping = new InstitutionUser();
        mapping.setUser(user);

        when(institutionRepo.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionUserRepo.findByInstitution(institution)).thenReturn(Collections.singletonList(mapping));

        // Act
        List<User> users = institutionUserService.getUsersForInstitution(institutionId);

        // Assert
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }
}
