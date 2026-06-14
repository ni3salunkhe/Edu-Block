package com.edu.servicesImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edu.config.MfaTokenStore;
import com.edu.dto.LoginRequest;
import com.edu.dto.LoginResponse;
import com.edu.entity.User;
import com.edu.repositories.UserRepository;
import com.edu.services.AuthService;
import com.edu.util.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MfaTokenStore mfaTokenStore;
    private final com.edu.repositories.AdminMfaRepository adminMfaRepo;

    public AuthServiceImpl(UserRepository userRepo,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           MfaTokenStore mfaTokenStore,
                           com.edu.repositories.AdminMfaRepository adminMfaRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mfaTokenStore = mfaTokenStore;
        this.adminMfaRepo = adminMfaRepo;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Block non-active accounts before issuing any token
        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("Your account has been suspended. Please contact support.");
        }
        if (user.getStatus() == User.Status.PENDING) {
            throw new RuntimeException("Your account is pending approval. Please wait for activation.");
        }

        if (user.getRole() == User.Role.ADMIN) {
            LoginResponse resp = new LoginResponse();
            resp.setRole("ADMIN");

            // Check if MFA is already setup and enabled
            boolean mfaEnabled = adminMfaRepo.findById(user.getId())
                    .map(com.edu.entity.AdminMfa::isEnabled)
                    .orElse(false);

            if (!mfaEnabled) {
                resp.setMfaSetupRequired(true);
            } else {
                resp.setMfaRequired(true);
            }

            String mfaToken = mfaTokenStore.createToken(user.getId());
            resp.setMfaToken(mfaToken);

            return resp;
        }

        // normal users
        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user));
        resp.setRole(user.getRole().name());
        resp.setMfaRequired(false);
        return resp;
    }

}
