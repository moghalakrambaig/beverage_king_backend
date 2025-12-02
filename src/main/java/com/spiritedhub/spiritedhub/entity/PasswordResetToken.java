package com.spiritedhub.spiritedhub.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    public String token;

    public String email;
    public LocalDateTime expiry;
}

