package com.edu.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.config.MfaTokenStore;
import com.edu.dto.LoginResponse;
import com.edu.dto.MfaVerifyRequest;
import com.edu.entity.AdminMfa;
import com.edu.entity.User;
import com.edu.repositories.AdminMfaRepository;
import com.edu.repositories.UserRepository;
import com.edu.util.JwtUtil;
import com.edu.util.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@RestController
@RequestMapping("/api/auth/admin/mfa")
public class AdminMfaController {

    private final AdminMfaRepository mfaRepo;
    private final TotpService totpService;
    
    @Autowired
    UserRepository userRepo;
    
    @Autowired
    AdminMfaRepository adminMfaRepo;
    
    @Autowired
    JwtUtil jwtUtil;
    
    @Autowired
    MfaTokenStore mfaTokenStore;

    public AdminMfaController(AdminMfaRepository mfaRepo,
                              TotpService totpService) {
        this.mfaRepo = mfaRepo;
        this.totpService = totpService;
    }

    @PostMapping("/setup")
    public Map<String, String> setupMfa(@RequestParam UUID adminId) {

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Not an admin user");
        }

        if (mfaRepo.existsById(adminId)) {
            throw new RuntimeException("MFA already configured");
        }

        GoogleAuthenticatorKey key = totpService.generateSecret();

        AdminMfa mfa = new AdminMfa();
        mfa.setUser(admin);                 // ✅ SET USER, NOT ID
        mfa.setSecret(key.getKey());
        mfa.setEnabled(false);
        mfa.setFailedAttempts(0);
        mfa.setCreatedAt(Instant.now());

        mfaRepo.save(mfa);                  // ✅ SINGLE, CLEAN SAVE

        return Map.of(
            "qrCodeUrl", key.getKey(),
            "manualKey", key.getKey()
        );
    }

    
    @PostMapping("/verify")
    public LoginResponse verify(@RequestBody MfaVerifyRequest req) {

        System.out.println("=== MFA VERIFICATION DEBUG ===");
        System.out.println("MFA Token: " + req.getMfaToken());
        System.out.println("OTP received: " + req.getOtp());
        
        UUID adminId = mfaTokenStore.consumeToken(req.getMfaToken());
        System.out.println("Admin ID from token: " + adminId);

        AdminMfa mfa = mfaRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("MFA not setup"));

        System.out.println("MFA Secret (first 10 chars): " + mfa.getSecret().substring(0, Math.min(10, mfa.getSecret().length())));
        System.out.println("Verifying OTP...");
        
        if (!totpService.verifyCode(mfa.getSecret(), req.getOtp())) {
            System.out.println("OTP verification FAILED");
            mfa.setFailedAttempts(mfa.getFailedAttempts() + 1);
            mfaRepo.save(mfa);
            throw new RuntimeException("Invalid OTP");
        }

        System.out.println("OTP verification SUCCESS");
        mfa.setEnabled(true);
        mfa.setFailedAttempts(0);
        mfa.setLastVerifiedAt(Instant.now());
        mfaRepo.save(mfa);

        User admin = userRepo.findById(adminId).orElseThrow();

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(admin));
        resp.setRole("ADMIN");
        resp.setMfaRequired(false);
        
        System.out.println("Returning response with token and role");
        return resp;
    }

}

