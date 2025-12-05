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

            boolean hasPasswordColumn = Arrays.stream(headers)
                    .anyMatch(h -> h.equalsIgnoreCase("password"));

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
                        dynamicFields.put("Email", value);
                        continue;
                    }

                    if (key.equalsIgnoreCase("password")) {
                        passwordPlain = value;
                        continue;
                    }

                    if (!value.isEmpty()) {
                        dynamicFields.put(key, parseValue(value));
                    }
                }

                Customer saved = createOrUpdateCustomer(email, dynamicFields, hasPasswordColumn ? passwordPlain : null);
                customers.add(saved);
            }
        }

        return customers;
    }

    // =========================
    // 2️⃣ JSON endpoint method
    // =========================
    public Customer createOrUpdateCustomer(Map<String, Object> body) {

        if (!body.containsKey("dynamicFields"))
            throw new IllegalArgumentException("dynamicFields object is required");

        Map<String, Object> dynamicFields = (Map<String, Object>) body.get("dynamicFields");

        String email = dynamicFields.get("Email") != null ? dynamicFields.get("Email").toString() : null;

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email field is required");

        String rawPassword = body.containsKey("password") ? body.get("password").toString() : null;

        return createOrUpdateCustomer(email, dynamicFields, rawPassword);
    }

    // =========================
    // 3️⃣ Used by both CSV + JSON
    // =========================
    private Customer createOrUpdateCustomer(String email, Map<String, Object> dynamicFields, String rawPassword) {

        Optional<Customer> existingCustomerOpt = customerRepository.findByDynamicFieldsEmail(email);
        Customer customer;

        if (existingCustomerOpt.isPresent()) {
            customer = existingCustomerOpt.get();

            // Do NOT overwrite fields with null or empty values
            dynamicFields.forEach((key, value) -> {
                if (value != null && !value.toString().isBlank()) {
                    customer.getDynamicFields().put(key, value);
                }
            });

        } else {
            customer = new Customer();
            customer.setDynamicFields(dynamicFields);
        }

        Customer savedCustomer = customerRepository.save(customer);

        // =========================
        // Password handling
        // =========================
        if (rawPassword != null && !rawPassword.isBlank()) {
            Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);
            PasswordAuth auth = authOpt.orElseGet(PasswordAuth::new);

            auth.setEmail(email);
            auth.setPasswordHash(passwordEncoder.encode(rawPassword));
            passwordAuthRepository.save(auth);
        }

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
        } catch (Exception ignored) {}

        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {}

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        return value;
    }
}
