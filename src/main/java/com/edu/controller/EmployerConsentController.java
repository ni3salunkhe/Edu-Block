package com.edu.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.ConsentRequestDTO;
import com.edu.dto.EmployerConsentListDTO;
import com.edu.entity.EmployerConsent;
import com.edu.entity.User;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.ConsentService;
import com.edu.util.SecurityUtil;

@RestController
@RequestMapping("/api/consent")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private EmployerConsentRepository consentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<Void> requestConsent(@RequestBody ConsentRequestDTO dto) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        consentService.requestConsent(employerId, dto.getIndividualId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employer")
    public List<EmployerConsentListDTO> getMyRequests() {
        UUID employerId = SecurityUtil.getCurrentUserId();
        
        return consentRepository.findByEmployerId(employerId).stream()
            .map(c -> {
                String individualName = userRepository.findById(c.getOwnerId())
                    .map(User::getUsername)
                    .orElse("Unknown");
                    
                return new EmployerConsentListDTO(c.getId(), individualName, c.getStatus());
            })
            .collect(Collectors.toList());
    }
}
