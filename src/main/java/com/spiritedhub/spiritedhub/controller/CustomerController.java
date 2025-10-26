package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // SIGN UP - Create customer
    // =========================
    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse("Email already exists", null)
            );
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer savedCustomer = customerRepository.save(customer);

        return ResponseEntity.ok(new ApiResponse("Customer created successfully", savedCustomer));
    }

    // =========================
    // CUSTOMER LOGIN
    // =========================
    @PostMapping("/auth/customer-login")
public ResponseEntity<?> customerLogin(@RequestParam String email, @RequestParam String password) {
    Optional<Customer> customerOpt = customerRepository.findByEmail(email);

    if (customerOpt.isEmpty() || !passwordEncoder.matches(password, customerOpt.get().getPassword())) {
        return ResponseEntity.status(401)
                .body(new ApiResponse("Invalid credentials", null));
    }

    return ResponseEntity.ok(new ApiResponse("Login successful", customerOpt.get()));
}


    // =========================
    // GET ALL CUSTOMERS
    // =========================
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(new ApiResponse("Customers fetched successfully", customers));
    }

    // =========================
    // GET CUSTOMER BY ID
    // =========================
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            return ResponseEntity.ok(new ApiResponse("Customer fetched successfully", customerOpt.get()));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }
    }

    // =========================
    // UPDATE CUSTOMER
    // =========================
    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }

        Customer customer = customerOpt.get();
        customer.setCus_name(customerDetails.getCus_name());
        customer.setMobile(customerDetails.getMobile());
        customer.setEmail(customerDetails.getEmail());
        customer.setPoints(customerDetails.getPoints());

        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", updatedCustomer));
    }

    // =========================
    // DELETE CUSTOMER
    // =========================
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(404).body(new ApiResponse("Customer not found", null));
        }
        customerRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("Customer deleted successfully", null));
    }

    // =========================
    // EXPORT CUSTOMERS TO CSV
    // =========================
    @GetMapping("/customers/export")
    public void exportCustomers(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");

        List<Customer> customers = customerRepository.findAll();
        StringBuilder csv = new StringBuilder("id,cus_name,mobile,email,points\n");

        for (Customer customer : customers) {
            csv.append(customer.getId()).append(",")
                    .append(customer.getCus_name()).append(",")
                    .append(customer.getMobile() != null ? customer.getMobile() : "").append(",")
                    .append(customer.getEmail()).append(",")
                    .append(customer.getPoints()).append("\n");
        }

        response.getWriter().write(csv.toString());
    }

    // =========================
    // Helper class for consistent API responses
    // =========================
    private static class ApiResponse {
        private String message;
        private Object data;

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
