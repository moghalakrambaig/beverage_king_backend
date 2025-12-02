package com.spiritedhub.spiritedhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email; // or customerId if you prefer

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, Instant expiryDate) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}
