package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
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
        if (customer.getDynamicFields() == null) return null;
        return customer.getDynamicFields().get(key);
    }

    @PostMapping("/signin")
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

    // Optionally: generate JWT or session info here
    return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "email", admin.getEmail()
    ));
}

    // ================= Forgot Password =================
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 1️⃣ Try Admin
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            String token = UUID.randomUUID().toString();
            admin.setResetPasswordToken(token);
            admin.setResetPasswordExpiry(Instant.now().plusSeconds(3600)); // 1 hour
            adminRepository.save(admin);

            String resetLink = "https://beverageking.vercel.app/reset-password?token=" + token;
            emailService.sendEmail(admin.getEmail(), "Admin Password Reset",
                    "Click this link to reset your admin password: " + resetLink);
        }

        // 2️⃣ Try Customer (dynamic)
        Optional<Customer> customerOpt = customerRepository.findByDynamicFieldsEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String token = UUID.randomUUID().toString();
            setCustomerField(customer, "resetPasswordToken", token);
            setCustomerField(customer, "resetPasswordExpiry", Instant.now().plusSeconds(3600));
            customerRepository.save(customer);

            String resetLink = "https://beverageking.vercel.app/reset-password?token=" + token;
            emailService.sendEmail((String) getCustomerField(customer, "email"),
                    "Customer Password Reset",
                    "Click this link to reset your customer account password: " + resetLink);
        }

        // Always return success (avoid email enumeration)
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    // ================= Reset Password =================
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        Instant now = Instant.now();

        // 1️⃣ Try Admin
        Optional<Admin> adminOpt = adminRepository.findByDynamicFieldsResetPasswordToken(token);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            if (admin.getResetPasswordExpiry() == null || admin.getResetPasswordExpiry().isBefore(now)) {
                return ResponseEntity.ok(Map.of("message", "Token has expired"));
            }

            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetPasswordToken(null);
            admin.setResetPasswordExpiry(null);
            adminRepository.save(admin);

            return ResponseEntity.ok(Map.of("message", "Admin password reset successfully"));
        }

        // 2️⃣ Try Customer (dynamic)
        Optional<Customer> customerOpt = customerRepository.findByDynamicFieldsResetPasswordToken(token);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Instant expiry = (Instant) getCustomerField(customer, "resetPasswordExpiry");

            if (expiry == null || expiry.isBefore(now)) {
                return ResponseEntity.ok(Map.of("message", "Token has expired"));
            }

            setCustomerField(customer, "password", passwordEncoder.encode(newPassword));
            setCustomerField(customer, "resetPasswordToken", null);
            setCustomerField(customer, "resetPasswordExpiry", null);

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
