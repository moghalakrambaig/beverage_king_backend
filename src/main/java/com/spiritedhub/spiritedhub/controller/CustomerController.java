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
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
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

    // =========================
    // CREATE CUSTOMER
    // =========================
    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        // Extract email from dynamicFields if available
        if (customer.getDynamicFields() != null && customer.getDynamicFields().get("email") != null) {
            String email = customer.getDynamicFields().get("email").toString();

            // Check if email already exists
            if (customerRepository.findByDynamicFieldsEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse("Email already exists", null));
            }

            // **Set it on the actual field** so it gets saved
            customer.setEmail(email);
        }

        // Set default password if not provided
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
    public ResponseEntity<ApiResponse> customerLogin(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        Optional<Customer> customerOpt = customerRepository.findByDynamicFieldsEmail(email);

        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(401).body(new ApiResponse("Invalid email", null));
        }

        Customer customer = customerOpt.get();

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return ResponseEntity.status(401).body(new ApiResponse("Invalid password", null));
        }

        return ResponseEntity.ok(new ApiResponse("Login successful", customer));
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

        try {
            List<Customer> customers = customerService.saveCustomersFromCsv(file);

            return ResponseEntity.ok(customers);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
