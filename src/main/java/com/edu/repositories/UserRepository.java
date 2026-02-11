package com.edu.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    long countByRole(User.Role role);
    
    // Find users by role (INDIVIDUAL) and username/email partial match
    List<User> findByRoleAndUsernameContainingIgnoreCase(User.Role role, String username);
    List<User> findByRoleAndEmailContainingIgnoreCase(User.Role role, String email);
}
