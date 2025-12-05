package com.spiritedhub.spiritedhub.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvValidationException;
import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.entity.PasswordAuth;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import com.spiritedhub.spiritedhub.repository.PasswordAuthRepository;
import com.spiritedhub.spiritedhub.service.CustomerService;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordAuthRepository passwordAuthRepository;

    // ---------------------------------------------------------
    // CREATE CUSTOMER
    // ---------------------------------------------------------
    @PostMapping("/upload-csv")
    public ResponseEntity<ApiResponse> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            List<Customer> customers = customerService.saveCustomersFromCsv(file);
            return ResponseEntity.ok(new ApiResponse("CSV processed successfully", customers));
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Failed to process CSV: " + e.getMessage(), null));
        }
    }

    // =========================
    // 2️⃣ Single customer via JSON
    // =========================
    @PostMapping
    public ResponseEntity<ApiResponse> createOrUpdateCustomer(@RequestBody Map<String, Object> body) {
        try {
            Customer customer = customerService.createOrUpdateCustomer(body);
            return ResponseEntity.ok(new ApiResponse("Customer processed successfully", customer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error: " + e.getMessage(), null));
        }
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @PostMapping("/customer-login")
    public ResponseEntity<?> customerLogin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password required"));

        Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);
        if (authOpt.isEmpty() || !passwordEncoder.matches(password, authOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        Optional<Customer> customerOpt = customerRepository.findById(authOpt.get().getCustomerId());
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Customer not found"));
        }

        return ResponseEntity.ok(Map.of("message", "Login successful", "data", customerOpt.get()));
    }

    // ---------------------------------------------------------
    // GET ALL CUSTOMERS
    // ---------------------------------------------------------
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse("Customers fetched successfully", customerRepository.findAll()));
    }

    // ---------------------------------------------------------
    // GET CUSTOMER BY ID
    // ---------------------------------------------------------
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {

        Optional<Customer> customerOpt = customerRepository.findById(id);

        return customerOpt
                .map(customer -> ResponseEntity.ok(new ApiResponse("Customer fetched successfully", customer)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse("Customer not found", null)));
    }

    // ---------------------------------------------------------
    // UPDATE CUSTOMER (NO PASSWORD LOGIC)
    // ---------------------------------------------------------
    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable String id, @RequestBody Customer customerDetails) {

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }

        Customer customer = customerOpt.get();

        if (customerDetails.getDynamicFields() != null) {
            customer.setDynamicFields(customerDetails.getDynamicFields());
        }

        // ❌ Password NOT handled here anymore

        Customer saved = customerRepository.save(customer);

        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", saved));
    }

    // ---------------------------------------------------------
    // DELETE CUSTOMER
    // ---------------------------------------------------------
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable String id) {

        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }

        // Delete credentials also
        passwordAuthRepository.deleteByCustomerId(id);

        customerRepository.deleteById(id);

        return ResponseEntity.ok(new ApiResponse("Customer deleted successfully", null));
    }

    // ---------------------------------------------------------
    // DELETE ALL CUSTOMERS
    // ---------------------------------------------------------
    @DeleteMapping("/customers")
    public ResponseEntity<?> deleteAllCustomers() {
        try {
            customerRepository.deleteAll();
            passwordAuthRepository.deleteAll();
            return ResponseEntity.ok(new ApiResponse("All customers deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to delete all customers: " + e.getMessage(), null));
        }
    }

    // ---------------------------------------------------------
    // CSV UPLOAD
    // ---------------------------------------------------------
    @PostMapping("/customers/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("CSV file is required", null));
        }

        try {
            List<Customer> customers = customerService.saveCustomersFromCsv(file);

            if (customers.isEmpty()) {
                return ResponseEntity
                        .ok(new ApiResponse("CSV uploaded but no valid rows found", Collections.emptyList()));
            }

            return ResponseEntity.ok(
                    new ApiResponse("CSV uploaded and processed successfully", customers));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Failed to process CSV: " + e.getMessage(), null));
        }
    }

    // ---------------------------------------------------------
    // Helper Class
    // ---------------------------------------------------------
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
