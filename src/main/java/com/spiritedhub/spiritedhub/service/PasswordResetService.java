package com.spiritedhub.spiritedhub.service;

import com.spiritedhub.spiritedhub.entity.PasswordResetToken;
import com.spiritedhub.spiritedhub.repository.PasswordResetTokenRepository;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.repository.PasswordAuthRepository;
import com.spiritedhub.spiritedhub.entity.PasswordAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository; // customer passwords

    @Autowired
    private AdminRepository adminRepository; // admin passwords

    @Autowired
    private PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------
    // CREATE TOKEN
    // -------------------------------------------------------------
    public String createToken(String email) {
        tokenRepository.deleteByEmail(email); // remove old tokens

        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, email, expiry);
        tokenRepository.save(resetToken);
        return token;
    }

    // -------------------------------------------------------------
    // VALIDATE TOKEN
    // -------------------------------------------------------------
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tOpt = tokenRepository.findByToken(token);

        if (tOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken t = tOpt.get();

        // auto delete expired token
        if (t.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(t);
            return false;
        }

        return true;
    }

    // -------------------------------------------------------------
    // RESET PASSWORD (CUSTOMER + ADMIN)
    // -------------------------------------------------------------
    public boolean resetPassword(String token, String newPassword) {

        Optional<PasswordResetToken> tOpt = tokenRepository.findByToken(token);
        if (tOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken t = tOpt.get();

        if (t.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(t);
            return false;
        }

        String email = t.getEmail();

        // 1️⃣ Check if customer exists in PasswordAuth table
        Optional<PasswordAuth> customerAuth = passwordAuthRepository.findByEmail(email);

        if (customerAuth.isPresent()) {
            PasswordAuth auth = customerAuth.get();
            auth.setPasswordHash(passwordEncoder.encode(newPassword));
            passwordAuthRepository.save(auth);
        } else {
            // 2️⃣ If not found → reset admin password
            var adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isEmpty())
                return false;

            var admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
        }

        // Delete used token
        tokenRepository.delete(t);

        return true;
    }
}
