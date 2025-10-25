package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // ================= Forgot Password =================
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            // Generate token and set expiry
            String token = UUID.randomUUID().toString();
            admin.setResetPasswordToken(token);
            admin.setResetPasswordExpiry(Instant.now().plusSeconds(3600)); // 1 hour expiry
            adminRepository.save(admin);

            // Send reset email
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            emailService.sendEmail(admin.getEmail(), "Admin Password Reset",
                    "Click this link to reset your password: " + resetLink);
        }

        // Always return success (prevent email enumeration)
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    // ================= Reset Password =================
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByResetPasswordToken(request.getToken());

        if (adminOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Invalid or expired token"));
        }

        Admin admin = adminOpt.get();

        if (admin.getResetPasswordExpiry().isBefore(Instant.now())) {
            return ResponseEntity.ok(Map.of("message", "Token has expired"));
        }

        // Update password and clear token & expiry
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setResetPasswordToken(null);
        admin.setResetPasswordExpiry(null);
        adminRepository.save(admin);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ================= Request DTO =================
    private static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public String getToken() {
            return token;
        }

        public String getNewPassword() {
            return newPassword;
        }
    }
}
