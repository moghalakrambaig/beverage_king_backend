package com.spiritedhub.spiritedhub.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.entity.PasswordAuth;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import com.spiritedhub.spiritedhub.repository.PasswordAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Save customers imported from CSV dynamically.
     * ALL columns are saved into dynamicFields automatically.
     * Passwords are stored ONLY in PasswordAuth table.
     */
    public List<Customer> saveCustomersFromCsv(MultipartFile file)
            throws IOException, CsvValidationException {

        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {

            String[] headers = reader.readNext();
            if (headers == null) return customers;

            String[] row;

            while ((row = reader.readNext()) != null) {

                Customer customer = new Customer();
                Map<String, Object> dynamicFields = new HashMap<>();

                String email = null;
                String passwordPlain = null;

                for (int i = 0; i < headers.length; i++) {

                    String key = headers[i].trim();
                    String value = i < row.length ? row[i].trim() : "";

                    // Extract Email column for PasswordAuth
                    if (key.equalsIgnoreCase("email")) {
                        email = value;
                        dynamicFields.put(key, value);
                        continue;
                    }

                    // Extract password column but DO NOT store in Customer
                    if (key.equalsIgnoreCase("password")) {
                        passwordPlain = value;
                        continue;
                    }

                    dynamicFields.put(key, parseValue(value));
                }

                // Save Customer first
                customer.setDynamicFields(dynamicFields);
                Customer savedCustomer = customerRepository.save(customer);

                // Default password if CSV has no password column
                if (passwordPlain == null || passwordPlain.isEmpty()) {
                    passwordPlain = "defaultPassword";
                }

                // Create PasswordAuth entry
                PasswordAuth auth = new PasswordAuth();
                auth.setEmail(email);
                auth.setCustomerId(savedCustomer.getId());
                auth.setPasswordHash(passwordEncoder.encode(passwordPlain));

                passwordAuthRepository.save(auth);

                customers.add(savedCustomer);
            }
        }

        return customers;
    }

    // =========================
    // Helper Methods
    // =========================
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) return null;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        return value;
    }

    private Instant parseInstant(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
