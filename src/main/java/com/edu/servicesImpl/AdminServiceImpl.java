package com.edu.servicesImpl;

import java.time.Instant;
import java.util.UUID;

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
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private AuditService auditService;

    public AdminServiceImpl(UserRepository userRepo,
                            VerifierProfileRepository verifierRepo,
                            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.verifierRepo = verifierRepo;
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
