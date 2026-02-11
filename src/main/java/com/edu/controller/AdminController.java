package com.edu.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.entity.User;
import com.edu.entity.VerifierProfile;
import com.edu.services.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/verifiers")
    public User createVerifier(@RequestBody VerifierProfile profile,
                               @RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password) {
        return adminService.createVerifier(profile, username, email, password);
    }

    @PostMapping("/users/{id}/suspend")
    public void suspendUser(@PathVariable UUID id) {
        adminService.suspendUser(id);
    }

    @PostMapping("/users/{id}/activate")
    public void activateUser(@PathVariable UUID id) {
        adminService.activateUser(id);
    }
}

