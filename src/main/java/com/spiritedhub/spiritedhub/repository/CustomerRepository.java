package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    // Find customer by email inside dynamicFields map
    @Query("{ 'dynamicFields.email': ?0 }")
    Optional<Customer> findByDynamicFieldsEmail(String email);

    // Find customer by reset password token inside dynamicFields map
    @Query("{ 'dynamicFields.resetPasswordToken': ?0 }")
    Optional<Customer> findByDynamicFieldsResetPasswordToken(String token);
}
