package com.spiritedhub.spiritedhub.controller;

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
    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, Object> body) {

        // Convert incoming payload into a Customer
        Customer customer = new Customer();

        if (!body.containsKey("dynamicFields")) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse("dynamicFields object is required", null));
        }

        Map<String, Object> dynamicFields = (Map<String, Object>) body.get("dynamicFields");
        customer.setDynamicFields(dynamicFields);

        if (!dynamicFields.containsKey("Email")) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse("Email field is required", null));
        }

        String email = dynamicFields.get("Email").toString();
        String rawPassword = body.containsKey("password")
                ? body.get("password").toString()
                : "defaultPassword";

        // 1. Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Create PasswordAuth entry
        PasswordAuth auth = new PasswordAuth();
        auth.setEmail(email);
        auth.setCustomerId(savedCustomer.getId());
        auth.setPasswordHash(passwordEncoder.encode(rawPassword));

        passwordAuthRepository.save(auth);

        return ResponseEntity.ok(new ApiResponse("Customer created", savedCustomer));
    }


    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @PostMapping("/auth/customer-login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse("Email and password are required", null));
        }

        Optional<PasswordAuth> authOpt = passwordAuthRepository.findByEmail(email);

        if (authOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("Invalid credentials", null));
        }

        PasswordAuth auth = authOpt.get();

        if (!passwordEncoder.matches(password, auth.getPasswordHash())) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("Invalid credentials", null));
        }

        Customer customer = customerRepository.findById(auth.getCustomerId()).orElse(null);

        return ResponseEntity.ok(new ApiResponse("Login successful", customer));
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

        if (file.isEmpty())
            return ResponseEntity.badRequest().body("File is empty");

        try {
            List<Customer> customers = customerService.saveCustomersFromCsv(file);
            return ResponseEntity.ok(customers);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
