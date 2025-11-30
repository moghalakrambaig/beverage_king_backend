package com.spiritedhub.spiritedhub.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spiritedhub.spiritedhub.entity.PasswordResetToken;
import com.spiritedhub.spiritedhub.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository repo;

    @Autowired
    private EmailService emailService;

    @Value("${FRONTEND_RESET_URL}")
    private String frontendResetUrl;

    public void createResetToken(String email) {

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = new PasswordResetToken();
        prt.token = token;
        prt.email = email;
        prt.expiry = LocalDateTime.now().plusMinutes(30);

        repo.save(prt);

        String resetLink = frontendResetUrl + "?token=" + token;

        emailService.sendPasswordResetEmail(email, resetLink);
    }
}
