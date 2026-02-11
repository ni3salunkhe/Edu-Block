package com.edu.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.EmployerSummaryDTO;
import com.edu.dto.UserSearchDTO;
import com.edu.entity.EmployerConsent;
import com.edu.entity.User;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.repositories.UserRepository;
import com.edu.util.SecurityUtil;

@RestController
@RequestMapping("/api/employer")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerController {

    @Autowired
    private EmployerConsentRepository consentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.edu.repositories.IndividualProfileRepository individualProfileRepository;

    @GetMapping("/summary")
    public EmployerSummaryDTO getSummary() {
        UUID employerId = SecurityUtil.getCurrentUserId();

        long pending = consentRepository.countByEmployerIdAndStatus(employerId, EmployerConsent.Status.REQUESTED);
        long approved = consentRepository.countByEmployerIdAndStatus(employerId, EmployerConsent.Status.APPROVED);
        long rejected = consentRepository.countByEmployerIdAndStatus(employerId, EmployerConsent.Status.REJECTED);

        return new EmployerSummaryDTO(pending, approved, rejected);
    }

    @GetMapping("/search")
    public List<UserSearchDTO> search(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Search by username OR email for role INDIVIDUAL
        // Since JPA multiple conditions is tricky with "OR" and "AND Role", we can do two queries and merge or use @Query.
        // For simplicity: Search username first, then email
        
        List<User> users = userRepository.findByRoleAndUsernameContainingIgnoreCase(User.Role.INDIVIDUAL, query);
        if (users.isEmpty()) {
            users = userRepository.findByRoleAndEmailContainingIgnoreCase(User.Role.INDIVIDUAL, query);
        }

        return users.stream()
                .map(u -> {
                    String fullName = individualProfileRepository.findById(u.getId())
                        .map(com.edu.entity.IndividualProfile::getFullName)
                        .orElse("");
                    return new UserSearchDTO(u.getId(), u.getUsername(), u.getEmail(), fullName);
                })
                .collect(Collectors.toList());
    }
}
