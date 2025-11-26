package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {

    @Query("{ 'dynamicFields.email' : ?0 }")
    Optional<Admin> findByEmail(String email);

    @Query("{ 'dynamicFields.resetPasswordToken' : ?0 }")
    Optional<Admin> findByDynamicFieldsResetPasswordToken(String token);
}