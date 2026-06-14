package com.edu.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.edu.entity.Institution;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;

public interface InstitutionUserService {
    InstitutionUser mapUserToInstitution(UUID userId, UUID institutionId);
    Optional<Institution> getInstitutionForUser(UUID userId);
    List<User> getUsersForInstitution(UUID institutionId);
    void removeUserFromInstitution(UUID userId);
}
