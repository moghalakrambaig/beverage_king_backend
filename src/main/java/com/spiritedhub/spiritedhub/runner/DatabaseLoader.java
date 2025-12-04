package com.spiritedhub.spiritedhub.runner;

import com.spiritedhub.spiritedhub.entity.Admin;
import com.spiritedhub.spiritedhub.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseLoader(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setEmail("admin@beverageking.com");
            admin.setPassword(passwordEncoder.encode("password"));
            adminRepository.save(admin);
        }
    }
}
