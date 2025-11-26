package com.spiritedhub.spiritedhub.controller;

import com.opencsv.CSVReader;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // CREATE CUSTOMER
    // =========================
    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        if (customer.getDynamicFields() != null && customer.getDynamicFields().get("email") != null) {
            String email = customer.getDynamicFields().get("email").toString();
            if (customerRepository.findByDynamicFieldsEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse("Email already exists", null));
            }
        }

        if (customer.getPassword() == null) {
            customer.setPassword(passwordEncoder.encode("defaultPassword"));
        }

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Customer created successfully", savedCustomer));
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/auth/customer-login")
    public ResponseEntity<?> customerLogin(@RequestParam String email, @RequestParam String password) {
        Optional<Customer> customerOpt = customerRepository.findByDynamicFieldsEmail(email);

        if (customerOpt.isEmpty() ||
                !passwordEncoder.matches(password, customerOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(new ApiResponse("Invalid credentials", null));
        }

        return ResponseEntity.ok(new ApiResponse("Login successful", customerOpt.get()));
    }

    // =========================
    // GET ALL CUSTOMERS
    // =========================
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse("Customers fetched successfully", customerRepository.findAll()));
    }

    // =========================
    // GET CUSTOMER BY ID
    // =========================
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        return customerOpt
                .map(customer -> ResponseEntity.ok(new ApiResponse("Customer fetched successfully", customer)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse("Customer not found", null)));
    }

    // =========================
    // UPDATE CUSTOMER
    // =========================
    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable String id, @RequestBody Customer customerDetails) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }

        Customer customer = customerOpt.get();

        if (customerDetails.getDynamicFields() != null)
            customer.setDynamicFields(customerDetails.getDynamicFields());

        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty())
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", saved));
    }

    // =========================
    // DELETE CUSTOMER
    // =========================
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable String id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }

        customerRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("Customer deleted successfully", null));
    }

    @DeleteMapping("/customers")
    public ResponseEntity<?> deleteAllCustomers() {
        try {
            customerRepository.deleteAll();
            return ResponseEntity.ok(new ApiResponse("All customers deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to delete all customers: " + e.getMessage(), null));
        }
    }

    // =========================
    // CSV UPLOAD (Fully Dynamic)
    // =========================
    @PostMapping("/customers/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body("File is empty");

        List<Customer> customers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null)
                return ResponseEntity.badRequest().body("CSV has no header");

            String[] headers = headerLine.split(",", -1);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);
                Customer c = new Customer();
                Map<String, Object> dynamicFields = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < columns.length ? columns[i].trim() : "";

                    // Password is special
                    if (key.equalsIgnoreCase("password")) {
                        c.setPassword(passwordEncoder.encode(value.isEmpty() ? "defaultPassword" : value));
                        continue;
                    }

                    // Add all other columns to dynamicFields
                    dynamicFields.put(key, value);
                }

                // Default password if missing
                if (c.getPassword() == null) {
                    c.setPassword(passwordEncoder.encode("defaultPassword"));
                }

                c.setDynamicFields(dynamicFields);
                customers.add(c);
            }

            customerRepository.saveAll(customers);
            return ResponseEntity.ok(customers);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload CSV: " + e.getMessage());
        }
    }

    private Object parseValue(String value) {
        if (value == null || value.isEmpty())
            return null;

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

    // =========================
    // Helper class
    // =========================
    private static class ApiResponse {
        private final String message;
        private final Object data;

        public ApiResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
