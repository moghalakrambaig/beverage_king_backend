package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import com.spiritedhub.spiritedhub.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private String frontendUrl = "https://beverageking.vercel.app";

    // ================= Forgot Password =================
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Admin
        adminRepository.findByEmail(email).ifPresent(admin -> {
            String token = UUID.randomUUID().toString();
            admin.setResetPasswordToken(token);
            admin.setResetPasswordExpiry(Instant.now().plusSeconds(3600));
            adminRepository.save(admin);

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            emailService.sendEmail(
                    admin.getEmail(),
                    "Admin Password Reset",
                    "<p>Click the link below to reset your admin password:</p>" +
                            "<a href=\"" + resetLink + "\">Reset Password</a>");
        });

        // Customer
        customerRepository.findByEmail(email).ifPresent(customer -> {
            String token = UUID.randomUUID().toString();
            customer.setResetPasswordToken(token);
            customer.setResetPasswordExpiry(Instant.now().plusSeconds(3600));
            customerRepository.save(customer);

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            emailService.sendEmail(
                    customer.getEmail(),
                    "Customer Password Reset",
                    "<p>Click the link below to reset your customer account password:</p>" +
                            "<a href=\"" + resetLink + "\">Reset Password</a>");
        });

        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    // ================= Reset Password =================
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        // Admin
        Optional<Admin> adminOpt = adminRepository.findByResetPasswordToken(token);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getResetPasswordExpiry().isBefore(Instant.now())) {
                return ResponseEntity.ok(Map.of("message", "Token has expired"));
            }
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetPasswordToken(null);
            admin.setResetPasswordExpiry(null);
            adminRepository.save(admin);
            return ResponseEntity.ok(Map.of("message", "Admin password reset successfully"));
        }

        // Customer
        Optional<Customer> customerOpt = customerRepository.findByResetPasswordToken(token);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (customer.getResetPasswordExpiry().isBefore(Instant.now())) {
                return ResponseEntity.ok(Map.of("message", "Token has expired"));
            }
            customer.setPassword(passwordEncoder.encode(newPassword));
            customer.setResetPasswordToken(null);
            customer.setResetPasswordExpiry(null);
            customerRepository.save(customer);
            return ResponseEntity.ok(Map.of("message", "Customer password reset successfully"));
        }

        return ResponseEntity.ok(Map.of("message", "Invalid or expired token"));
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
