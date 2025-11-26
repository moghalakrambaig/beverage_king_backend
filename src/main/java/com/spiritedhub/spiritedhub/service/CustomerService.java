package com.spiritedhub.spiritedhub.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
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
    private PasswordEncoder passwordEncoder;

    /**
     * Save customers imported from CSV dynamically
     * ANY column will be saved automatically in dynamicFields
     */
    public List<Customer> saveCustomersFromCsv(MultipartFile file)
            throws IOException, CsvValidationException {

        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader =
                     new CSVReader(new InputStreamReader(file.getInputStream()))) {

            String[] headerRow = reader.readNext(); // read column names
            if (headerRow == null) return customers;

            while (true) {
                String[] row = reader.readNext();
                if (row == null) break;

                Customer customer = new Customer();
                Map<String, Object> dynamic = new HashMap<>();

                for (int i = 0; i < headerRow.length; i++) {
                    String column = headerRow[i].trim();
                    String value = i < row.length ? row[i].trim() : "";

                    // ================================
                    // Map known fields (optional)
                    // CSV columns can be anything
                    // ================================
                    switch (column.toLowerCase()) {
                        case "name" -> customer.setName(value);
                        case "phone" -> customer.setPhone(value);
                        case "email" -> customer.setEmail(value);
                        case "password" -> customer.setPassword(passwordEncoder.encode(value));
                        case "signupdate" -> customer.setSignUpDate(parseInstant(value));
                        default -> dynamic.put(column, parseValue(value));
                    }
                }

                // Default password if missing
                if (customer.getPassword() == null) {
                    customer.setPassword(passwordEncoder.encode("defaultPassword"));
                }

                customer.setDynamicFields(dynamic);

                customers.add(customer);
            }
        }

        return customerRepository.saveAll(customers);
    }

    // ======================
    // Helper Methods
    // ======================

    private Instant parseInstant(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Auto-detect and convert numeric/boolean fields
     */
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) return null;

        // try integer
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {}

        // try decimal
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {}

        // try boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        return value; // fallback to string
    }
}
