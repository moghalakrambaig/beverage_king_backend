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
import java.util.*;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // 1️⃣ CSV upload method
    // =========================
    public List<Customer> saveCustomersFromCsv(MultipartFile file) throws IOException, CsvValidationException {
        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null)
                return customers;

            String[] row;
            while ((row = reader.readNext()) != null) {
                Map<String, Object> dynamicFields = new HashMap<>();
                String email = null;
                String passwordPlain = null;

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < row.length ? row[i].trim() : "";

                    if (key.equalsIgnoreCase("email")) {
                        email = value;
                        dynamicFields.put(key, value);
                        continue;
                    }
                    if (key.equalsIgnoreCase("password")) {
                        passwordPlain = value;
                        continue;
                    }

                    dynamicFields.put(key, parseValue(value));
                }

                Customer customer = createOrUpdateCustomer(email, dynamicFields, passwordPlain);
                customers.add(customer);
            }
        }

        return customers;
    }

    // =========================
    // 2️⃣ API JSON payload method
    // =========================
    public Customer createOrUpdateCustomer(Map<String, Object> body) {
        if (!body.containsKey("dynamicFields")) {
            throw new IllegalArgumentException("dynamicFields object is required");
        }
        Map<String, Object> dynamicFields = (Map<String, Object>) body.get("dynamicFields");
        String email = (String) dynamicFields.get("Email");
        String rawPassword = body.containsKey("password") ? body.get("password").toString() : null;

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email field is required");
        }

        return createOrUpdateCustomer(email, dynamicFields, rawPassword);
    }

    // =========================
    // 3️⃣ Internal helper method used by both CSV & JSON
    // =========================
    private Customer createOrUpdateCustomer(String email, Map<String, Object> dynamicFields, String rawPassword) {
        Optional<Customer> existingCustomerOpt = customerRepository.findByDynamicFieldsEmail(email);
        Customer customer;

        if (existingCustomerOpt.isPresent()) {
            // Update existing customer fields
            customer = existingCustomerOpt.get();
            customer.getDynamicFields().putAll(dynamicFields);
        } else {
            // Create new customer
            customer = new Customer();
            customer.setDynamicFields(dynamicFields);
        }

        Customer savedCustomer = customerRepository.save(customer);

        // Password handling
        Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);
        PasswordAuth auth;
        if (authOpt.isPresent()) {
            auth = authOpt.get();
            if (rawPassword != null && !rawPassword.isBlank()) {
                auth.setPasswordHash(passwordEncoder.encode(rawPassword));
            }
        } else {
            auth = new PasswordAuth();
            auth.setEmail(email);
            auth.setPasswordHash(passwordEncoder.encode(
                    rawPassword != null && !rawPassword.isBlank() ? rawPassword : "defaultPassword"));
        }
        passwordAuthRepository.save(auth);

        return savedCustomer;
    }

    // =========================
    // Helper for value parsing
    // =========================
    private Object parseValue(String value) {
        if (value == null || value.isEmpty())
            return null;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        return value;
    }
}
