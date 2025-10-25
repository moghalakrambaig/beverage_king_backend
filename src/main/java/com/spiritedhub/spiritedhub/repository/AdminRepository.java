package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByResetPasswordToken(String token); // New method
}
