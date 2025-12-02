package com.spiritedhub.spiritedhub.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.spiritedhub.spiritedhub.entity.PasswordAuth;

public interface PasswordAuthRepository extends MongoRepository<PasswordAuth, String> {
    Optional<PasswordAuth> findByEmail(String email);

    Optional<PasswordAuth> deleteByCustomerId(String customerId);
}