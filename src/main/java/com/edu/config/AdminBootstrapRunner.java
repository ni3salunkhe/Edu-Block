package com.edu.config;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.edu.entity.User;
import com.edu.repositories.UserRepository;

@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepo,
                                PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.findByUsername(adminUsername).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.Status.ACTIVE);
        admin.setCreatedAt(Instant.now());

        userRepo.save(admin);
    }
}
