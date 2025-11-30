package com.spiritedhub.spiritedhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spiritedhub.spiritedhub.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
}
