package com.spiritedhub.spiritedhub.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.spiritedhub.spiritedhub.entity.Customer;
import com.spiritedhub.spiritedhub.repository.CustomerRepository;

import jakarta.servlet.http.HttpServletResponse;

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
            return ResponseEntity.badRequest().body(new ApiResponse("Email already exists", null));
        }

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }

        if (customer.getSignUpDate() == null) {
            customer.setSignUpDate(LocalDate.now());
        }

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
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        return customerOpt.<ResponseEntity<?>>map(customer -> ResponseEntity.ok(new ApiResponse("Customer fetched successfully", customer)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse("Customer not found", null)));
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
        customer.setDisplayId(customerDetails.getDisplayId());
        customer.setName(customerDetails.getName());
        customer.setPhone(customerDetails.getPhone());
        customer.setEmail(customerDetails.getEmail());
        customer.setSignUpDate(customerDetails.getSignUpDate());
        customer.setEarnedPoints(customerDetails.getEarnedPoints());
        customer.setTotalVisits(customerDetails.getTotalVisits());
        customer.setTotalSpend(customerDetails.getTotalSpend());
        customer.setLastPurchaseDate(customerDetails.getLastPurchaseDate());
        customer.setEmployee(customerDetails.isEmployee());
        customer.setStartDate(customerDetails.getStartDate());
        customer.setEndDate(customerDetails.getEndDate());
        customer.setInternalLoyaltyCustomerId(customerDetails.getInternalLoyaltyCustomerId());

        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
        }

        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", customerRepository.save(customer)));
    }

    // =========================
    // DELETE CUSTOMER BY ID
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
        StringBuilder csv = new StringBuilder("Display ID,Name,Phone,Email,Sign Up Date,Earned Points,Total Visits,Total Spend,Last Purchase Date,Is Employee,Start Date,End Date,internal_loyalty_customer_id\n");

        for (Customer customer : customers) {
            csv.append(safe(customer.getDisplayId())).append(",")
               .append(safe(customer.getName())).append(",")
               .append(safe(customer.getPhone())).append(",")
               .append(safe(customer.getEmail())).append(",")
               .append(safe(customer.getSignUpDate())).append(",")
               .append(safe(customer.getEarnedPoints())).append(",")
               .append(safe(customer.getTotalVisits())).append(",")
               .append(safe(customer.getTotalSpend())).append(",")
               .append(safe(customer.getLastPurchaseDate())).append(",")
               .append(safe(customer.isEmployee())).append(",")
               .append(safe(customer.getStartDate())).append(",")
               .append(safe(customer.getEndDate())).append(",")
               .append(safe(customer.getInternalLoyaltyCustomerId())).append("\n");
        }

        response.getWriter().write(csv.toString());
    }

    // =========================
    // UPLOAD CUSTOMERS VIA CSV
    // =========================
    @PostMapping("/customers/upload-csv")
    public ResponseEntity<List<Customer>> uploadCSV(@RequestParam("file") MultipartFile file) {
        List<Customer> customers = new ArrayList<>();
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] columns = line.split(",", -1);
                if (columns.length < 13) continue;

                try {
                    Customer customer = new Customer();
                    customer.setDisplayId(columns[0]);
                    customer.setName(columns[1]);
                    customer.setPhone(columns[2]);
                    customer.setEmail(columns[3]);
                    customer.setSignUpDate(parseDate(columns[4], dateFormatter));
                    customer.setEarnedPoints(columns[5].isEmpty() ? 0 : Integer.parseInt(columns[5]));
                    customer.setTotalVisits(columns[6].isEmpty() ? 0 : Integer.parseInt(columns[6]));
                    customer.setTotalSpend(columns[7].isEmpty() ? 0.0 : Double.parseDouble(columns[7]));
                    customer.setLastPurchaseDate(parseDate(columns[8], dateFormatter));
                    customer.setEmployee(columns[9].equalsIgnoreCase("true"));
                    customer.setStartDate(parseDate(columns[10], dateFormatter));
                    customer.setEndDate(parseDate(columns[11], dateFormatter));
                    customer.setInternalLoyaltyCustomerId(columns[12]);
                    customer.setPassword(passwordEncoder.encode("defaultPassword"));

                    customers.add(customer);
                } catch (Exception e) {
                    System.out.println("Skipping row due to parsing error: " + line);
                }
            }

            customerRepository.saveAll(customers);
            return ResponseEntity.ok(customers);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try { return LocalDate.parse(dateStr, formatter); } 
        catch (DateTimeParseException e) { return null; }
    }

    private String safe(Object val) { return val == null ? "" : val.toString(); }

    private static class ApiResponse {
        private final String message;
        private final Object data;

        public ApiResponse(String message, Object data) { this.message = message; this.data = data; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}
