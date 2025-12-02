package com.spiritedhub.spiritedhub.service;

import com.spiritedhub.spiritedhub.entity.PasswordResetToken;
import com.spiritedhub.spiritedhub.repository.PasswordResetTokenRepository;
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
    private PasswordAuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String createToken(String email) {
        // Remove old tokens
        tokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, email, expiry);
        tokenRepository.save(resetToken);
        return token;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> t = tokenRepository.findByToken(token);
        return t.isPresent() && t.get().getExpiryDate().isAfter(Instant.now());
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tOpt = tokenRepository.findByToken(token);
        if (tOpt.isEmpty() || tOpt.get().getExpiryDate().isBefore(Instant.now())) {
            return false;
        }

        String email = tOpt.get().getEmail();
        Optional<PasswordAuth> authOpt = authRepository.findByEmail(email);
        if (authOpt.isPresent()) {
            PasswordAuth auth = authOpt.get();
            auth.setPasswordHash(passwordEncoder.encode(newPassword));
            authRepository.save(auth);
        }

        tokenRepository.delete(tOpt.get());
        return true;
    }
}
