package com.spiritedhub.spiritedhub.service;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.entity.PasswordAuth;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import com.spiritedhub.spiritedhub.repository.PasswordAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1️⃣ Try Admin Login
        Optional<Admin> adminOpt = adminRepository.findById(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword()) // Already bcrypt
                    .roles("ADMIN")
                    .build();
        }

        // 2️⃣ Try Customer Login
        Optional<Customer> customerOpt = customerRepository.findById(email);

        if (customerOpt.isPresent()) {

            Customer customer = customerOpt.get();

            // Fetch password from PasswordAuth table
            Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);

            if (authOpt.isEmpty()) {
                throw new UsernameNotFoundException("Password not found for email: " + email);
            }

            PasswordAuth auth = authOpt.get();

            return User.builder()
                    .username(email)
                    .password(auth.getPasswordHash()) // bcrypt hash
                    .roles("CUSTOMER")
                    .build();
        }

        // 3️⃣ Not found
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
