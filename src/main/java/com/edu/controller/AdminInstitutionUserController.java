package com.edu.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.edu.entity.InstitutionUser;
import com.edu.services.InstitutionUserService;

@RestController
@RequestMapping("/api/admin/institutions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstitutionUserController {

    private final InstitutionUserService institutionUserService;

    public AdminInstitutionUserController(InstitutionUserService institutionUserService) {
        this.institutionUserService = institutionUserService;
    }

    @PostMapping("/{institutionId}/users/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void mapUserToInstitution(@PathVariable UUID institutionId, @PathVariable UUID userId) {
        institutionUserService.mapUserToInstitution(userId, institutionId);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromInstitution(@PathVariable UUID userId) {
        institutionUserService.removeUserFromInstitution(userId);
    }
}
