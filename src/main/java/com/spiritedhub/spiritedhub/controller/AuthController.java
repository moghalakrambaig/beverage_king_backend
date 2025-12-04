package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.entity.PasswordAuth;
import com.spiritedhub.spiritedhub.entity.PasswordResetToken;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import com.spiritedhub.spiritedhub.repository.PasswordAuthRepository;
import com.spiritedhub.spiritedhub.repository.PasswordResetTokenRepository;
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
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // ================= Dynamic field helpers =================
    private void setCustomerField(Customer customer, String key, Object value) {
        if (customer.getDynamicFields() == null) {
            customer.setDynamicFields(new java.util.HashMap<>());
        }
        customer.getDynamicFields().put(key, value);
    }

    private Object getCustomerField(Customer customer, String key) {
        if (customer.getDynamicFields() == null)
            return null;
        return customer.getDynamicFields().get(key);
    }

    // ================= Admin Login =================
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of("message", "Login successful", "email", admin.getEmail()));
    }

    // ================= Forgot Password =================
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        Instant now = Instant.now();

        // Remove existing tokens
        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        Instant expiry = now.plusSeconds(3600); // 1 hour

        PasswordResetToken resetToken = new PasswordResetToken(token, email, expiry);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "https://beverageking.vercel.app/reset-password?token=" + token;
        String htmlBody = "<h2>Password Reset</h2>"
                + "<p>Click the link below to reset your password:</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>"
                + "<br><br><p>If you did not request this, ignore this email.</p>";

        emailService.sendHtmlEmail(email, "Password Reset", htmlBody);

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    // ================= Reset Password =================
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        Instant now = Instant.now();

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiryDate().isBefore(now)) {
            return ResponseEntity.ok(Map.of("message", "Invalid or expired token"));
        }

        String email = tokenOpt.get().getEmail();

        // Update password for customer
        Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);
        if (authOpt.isPresent()) {
            PasswordAuth auth = authOpt.get();
            auth.setPasswordHash(passwordEncoder.encode(newPassword));
            passwordAuthRepository.save(auth);
        }

        // Optionally, also check Admin entity if needed
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
        }

        // Delete token after use
        passwordResetTokenRepository.delete(tokenOpt.get());

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
