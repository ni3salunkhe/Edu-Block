package com.edu.services;

import java.util.Optional;
import java.util.UUID;

import com.edu.entity.EmployerProfile;
import com.edu.entity.IndividualProfile;
import com.edu.entity.InstitutionProfile;
import com.edu.entity.User;
import com.edu.entity.VerifierProfile;

public interface UserService {

    User registerIndividual(IndividualProfile profile, String username, String email, String rawPassword);

    User registerEmployer(EmployerProfile profile, String username, String email, String rawPassword);

    User registerInstitution(InstitutionProfile profile, String username, String email, String rawPassword);

    User createVerifier(VerifierProfile profile, String username, String email, String rawPassword);

    Optional<User> findById(UUID userId);
}
