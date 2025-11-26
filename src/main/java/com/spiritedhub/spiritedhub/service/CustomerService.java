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
     * Save customers imported from CSV dynamically.
     * ALL columns are saved into dynamicFields automatically.
     */
    public List<Customer> saveCustomersFromCsv(MultipartFile file)
            throws IOException, CsvValidationException {

        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {

            String[] headers = reader.readNext(); // read CSV column names
            if (headers == null) return customers;

            String[] row;
            while ((row = reader.readNext()) != null) {
                Customer customer = new Customer();
                Map<String, Object> dynamicFields = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < row.length ? row[i].trim() : "";

                    // Treat password specially
                    if (key.equalsIgnoreCase("password")) {
                        customer.setPassword(passwordEncoder.encode(value.isEmpty() ? "defaultPassword" : value));
                        continue;
                    }

                    // All other columns go into dynamicFields with auto-detection
                    dynamicFields.put(key, parseValue(value));
                }

                // Default password if not set
                if (customer.getPassword() == null) {
                    customer.setPassword(passwordEncoder.encode("defaultPassword"));
                }

                customer.setDynamicFields(dynamicFields);
                customers.add(customer);
            }
        }

        return customerRepository.saveAll(customers);
    }

    // =========================
    // Helper Methods
    // =========================

    /**
     * Auto-detect type: integer, double, boolean, or string
     */
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) return null;

        // Try integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        // Try double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}

        // Try boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        // fallback to string
        return value;
    }

    /**
     * Optional: parse date strings to Instant
     */
    private Instant parseInstant(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
