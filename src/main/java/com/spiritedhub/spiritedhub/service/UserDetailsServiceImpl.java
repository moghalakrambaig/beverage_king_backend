package com.spiritedhub.spiritedhub.service;

import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find customer by dynamicFields.email
        Optional<Customer> customerOpt = customerRepository.findByDynamicFieldsEmail(email);

        Customer customer = customerOpt.orElseThrow(
                () -> new UsernameNotFoundException("Customer not found with email: " + email)
        );

        String password = customer.getPassword();

        return User.builder()
                .username(email)
                .password(password)
                .roles("CUSTOMER") // you can extend for roles later
                .build();
    }
}
