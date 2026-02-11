package com.edu.services;

import java.util.UUID;

import com.edu.entity.User;
import com.edu.entity.VerifierProfile;

public interface AdminService {

    User createVerifier(VerifierProfile profile,
                        String username,
                        String email,
                        String password);

    void suspendUser(UUID userId);

    void activateUser(UUID userId);
}
