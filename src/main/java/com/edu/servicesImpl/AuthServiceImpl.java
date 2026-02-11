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

    public AuthServiceImpl(UserRepository userRepo,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           MfaTokenStore mfaTokenStore) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
		this.mfaTokenStore = mfaTokenStore;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getRole() == User.Role.ADMIN) {

            LoginResponse resp = new LoginResponse();
            resp.setMfaRequired(true);
            resp.setRole("ADMIN");

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
