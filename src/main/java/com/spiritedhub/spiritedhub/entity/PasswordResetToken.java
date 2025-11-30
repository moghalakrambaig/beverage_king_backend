package com.spiritedhub.spiritedhub.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PasswordResetToken {
    @Id
    public String token;

    public String email;
    public LocalDateTime expiry;
}

