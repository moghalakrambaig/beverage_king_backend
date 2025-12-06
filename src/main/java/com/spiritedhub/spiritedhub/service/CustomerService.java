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

    // CSV upload: delete all customers, preserve passwords
    public List<Customer> saveCustomersFromCsv(MultipartFile file)
            throws IOException, CsvValidationException {

        // DELETE ONLY CUSTOMER TABLE
        customerRepository.deleteAll();

        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {

            String[] headers = reader.readNext();
            if (headers == null)
                return customers;

            String[] row;

            while ((row = reader.readNext()) != null) {

                Map<String, Object> dynamic = new HashMap<>();
                String email = null;

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < row.length ? row[i].trim() : "";

                    if (key.equalsIgnoreCase("email")) {
                        email = value;
                        dynamic.put("Email", value);
                        continue;
                    }

                    if (!value.isEmpty()) {
                        dynamic.put(key, parseValue(value));
                    }
                }

                if (email == null || email.isBlank()) {
                    continue; // skip rows without email (invalid)
                }

                final String customerEmail = email; // ✅ effectively final for inner usage

                // 1️⃣ save to customers table
                Customer customer = new Customer();
                customer.setId(customerEmail); // use email as id
                customer.setDynamicFields(dynamic);

                Customer savedCustomer = customerRepository.save(customer);
                customers.add(savedCustomer);

                // 2️⃣ update password table (ADD ONLY NEW)
                Optional<PasswordAuth> existing = passwordAuthRepository.findByEmail(customerEmail);

                if (existing.isEmpty()) {
                    PasswordAuth auth = new PasswordAuth();
                    auth.setEmail(customerEmail);
                    auth.setPasswordHash(passwordEncoder.encode("Default@123")); // default password
                    passwordAuthRepository.save(auth);
                }
            }
        }

        return customers;
    }

    // Helper to parse numbers/boolean
    private Object parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);
        return value;
    }
}
