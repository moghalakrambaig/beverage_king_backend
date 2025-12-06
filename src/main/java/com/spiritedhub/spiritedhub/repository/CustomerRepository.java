package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    // Find customer by Email stored inside dynamicFields.Email
    @Query("{ 'dynamicFields.Email' : ?0 }, {'dynamicFields.email' : ?0}")
    Optional<Customer> findByEmail(String email);

    // Used for reset password token lookup
    @Query("{ 'dynamicFields.resetPasswordToken' : ?0 }")
    Optional<Customer> findByDynamicFieldsResetPasswordToken(String token);
}
