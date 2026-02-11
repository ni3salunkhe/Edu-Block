package com.edu.servicesImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edu.entity.EmployerProfile;
import com.edu.entity.IndividualProfile;
import com.edu.entity.InstitutionProfile;
import com.edu.entity.User;
import com.edu.entity.VerifierProfile;
import com.edu.repositories.EmployerProfileRepository;
import com.edu.repositories.IndividualProfileRepository;
import com.edu.repositories.InstitutionProfileRepository;
import com.edu.repositories.UserRepository;
import com.edu.repositories.VerifierProfileRepository;
import com.edu.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final IndividualProfileRepository individualRepo;
    private final EmployerProfileRepository employerRepo;
    private final InstitutionProfileRepository institutionRepo;
    private final VerifierProfileRepository verifierRepo;

    public UserServiceImpl(
        UserRepository userRepo,
        PasswordEncoder passwordEncoder,
        IndividualProfileRepository individualRepo,
        EmployerProfileRepository employerRepo,
        InstitutionProfileRepository institutionRepo,
        VerifierProfileRepository verifierRepo
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.individualRepo = individualRepo;
        this.employerRepo = employerRepo;
        this.institutionRepo = institutionRepo;
        this.verifierRepo = verifierRepo;
    }

    private User createUser(String username, String email, String rawPassword, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(User.Status.ACTIVE);
        user.setCreatedAt(Instant.now());
        return userRepo.save(user);
    }

    @Override
    public User registerIndividual(IndividualProfile profile, String username, String email, String rawPassword) {
        User user = createUser(username, email, rawPassword, User.Role.INDIVIDUAL);
        profile.setUser(user);
        individualRepo.save(profile);
        return user;
    }

    @Override
    public User registerEmployer(EmployerProfile profile, String username, String email, String rawPassword) {
        User user = createUser(username, email, rawPassword, User.Role.EMPLOYER);
        profile.setUser(user);
        employerRepo.save(profile);
        return user;
    }

    @Override
    public User registerInstitution(InstitutionProfile profile, String username, String email, String rawPassword) {
        User user = createUser(username, email, rawPassword, User.Role.INSTITUTION);
        profile.setUser(user);
        institutionRepo.save(profile);
        return user;
    }

    @Override
    public User createVerifier(VerifierProfile profile, String username, String email, String rawPassword) {
        User user = createUser(username, email, rawPassword, User.Role.VERIFIER);
        profile.setUser(user);
        verifierRepo.save(profile);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userRepo.findById(userId);
    }
}
