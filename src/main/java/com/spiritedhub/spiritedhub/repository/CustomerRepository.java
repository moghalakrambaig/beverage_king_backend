package com.spiritedhub.spiritedhub.repository;

import com.spiritedhub.spiritedhub.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    Optional<Customer> findById(String email);

    @Query("{ 'dynamicFields.resetPasswordToken' : ?0 }")
    Optional<Customer> findByDynamicFieldsResetPasswordToken(String token);
}
