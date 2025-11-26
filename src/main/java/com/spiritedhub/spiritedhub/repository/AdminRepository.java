package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    
    // Find Admin by email inside dynamicFields map (if you made Admin dynamic)
    Optional<Admin> findByDynamicFieldsEmail(String email);

    // Find Admin by reset password token inside dynamicFields map
    Optional<Admin> findByDynamicFieldsResetPasswordToken(String token);
}
