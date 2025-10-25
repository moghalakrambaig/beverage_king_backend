package com.spiritedhub.spiritedhub.controller;

import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // Get all customers
    // =========================
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // =========================
    // Create customer / Signup
    // =========================
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        // Check if email exists
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Hash password
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    // =========================
    // Get customer by ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            return ResponseEntity.ok(customerOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================
    // Update customer
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (!customerOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Customer customer = customerOpt.get();
        customer.setCus_name(customerDetails.getCus_name());
        customer.setMobile(customerDetails.getMobile());
        customer.setEmail(customerDetails.getEmail());
        customer.setPoints(customerDetails.getPoints());

        // Update password if provided
        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    // =========================
    // Delete customer
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // Export customers as CSV
    // =========================
    @GetMapping("/export")
    public void exportCustomers(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");

        List<Customer> customers = customerRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("id,cus_name,mobile,email,points\n");
        for (Customer customer : customers) {
            csv.append(customer.getId()).append(",")
                    .append(customer.getCus_name()).append(",")
                    .append(customer.getMobile() != null ? customer.getMobile() : "").append(",")
                    .append(customer.getEmail()).append(",")
                    .append(customer.getPoints()).append("\n");
        }
        response.getWriter().write(csv.toString());
    }
}
