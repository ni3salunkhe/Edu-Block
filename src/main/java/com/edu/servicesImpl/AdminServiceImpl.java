package com.edu.servicesImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.edu.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.edu.entity.User;
import com.edu.entity.VerifierProfile;
import com.edu.repositories.UserRepository;
import com.edu.repositories.VerifierProfileRepository;
import com.edu.services.AdminService;
import com.edu.services.AuditService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepo;
    private final VerifierProfileRepository verifierRepo;
    private final com.edu.repositories.EmployerConsentRepository employerConsentRepo;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private AuditService auditService;

    public AdminServiceImpl(UserRepository userRepo,
                            VerifierProfileRepository verifierRepo,
                            com.edu.repositories.EmployerConsentRepository employerConsentRepo,
                            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.verifierRepo = verifierRepo;
        this.employerConsentRepo = employerConsentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createVerifier(VerifierProfile profile,
                               String username,
                               String email,
                               String password) {

        User verifier = new User();
        verifier.setUsername(username);
        verifier.setEmail(email);
        verifier.setPasswordHash(passwordEncoder.encode(password));
        verifier.setRole(User.Role.VERIFIER);
        verifier.setStatus(User.Status.ACTIVE);
        verifier.setCreatedAt(Instant.now());

        verifier = userRepo.save(verifier);

        UUID adminId = UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestIp = request.getRemoteAddr();

        auditService.logAdminAction(
        	    adminId,
        	    "CREATE_VERIFIER",
        	    "USER",
        	    verifier.getId(),
        	    "{\"username\":\"" + verifier.getUsername() + "\"}",
        	    requestIp
        	);
        profile.setUser(verifier);
        verifierRepo.save(profile);

        return verifier;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getStatus() == User.Status.ACTIVE
                ))
                .collect(Collectors.toList());
    }

    @Override
    public com.edu.dto.AdminSummaryDTO getAdminSummary() {
        long totalUsers = userRepo.count();
        long verifiers = userRepo.countByRole(User.Role.VERIFIER);
        long activeConsents = employerConsentRepo.countByStatus(com.edu.entity.EmployerConsent.Status.APPROVED);

        return new com.edu.dto.AdminSummaryDTO(totalUsers, verifiers, activeConsents);
    }

    @Override
    public void suspendUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Admin cannot be suspended");
        }

        user.setStatus(User.Status.SUSPENDED);
        userRepo.save(user);
    }

    @Override
    public void activateUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(User.Status.ACTIVE);
        userRepo.save(user);
    }
}
