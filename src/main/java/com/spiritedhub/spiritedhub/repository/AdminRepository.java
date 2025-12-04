package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByResetPasswordToken(String token);
}