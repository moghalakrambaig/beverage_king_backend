package com.spiritedhub.spiritedhub.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // SIGN UP
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
            customer.setSignUpDate(Instant.now());
        }

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Customer created successfully", savedCustomer));
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/auth/customer-login")
    public ResponseEntity<?> customerLogin(@RequestParam String email, @RequestParam String password) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

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
    // (Mongo uses String id)
    // =========================
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {

        Optional<Customer> customerOpt = customerRepository.findById(id);

        return customerOpt.map(customer ->
                        ResponseEntity.ok(new ApiResponse("Customer fetched successfully", customer)))
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

        customer.setCurrentRank(customerDetails.getCurrentRank());
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

        // dynamic fields (new)
        customer.setDynamicFields(customerDetails.getDynamicFields());

        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
        }

        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", customerRepository.save(customer)));
    }

    // =========================
    // DELETE BY ID
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
    // EXPORT
    // =========================
    @GetMapping("/customers/export")
    public void exportCustomers(HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");

        List<Customer> customers = customerRepository.findAll();

        StringBuilder csv = new StringBuilder(
                "CurrentRank,DisplayID,Name,Phone,Email,SignUpDate,EarnedPoints,TotalVisits,TotalSpend,LastPurchase,IsEmployee,StartDate,EndDate,InternalID\n");

        for (Customer c : customers) {
            csv.append(safe(c.getCurrentRank())).append(",")
                    .append(safe(c.getDisplayId())).append(",")
                    .append(safe(c.getName())).append(",")
                    .append(safe(c.getPhone())).append(",")
                    .append(safe(c.getEmail())).append(",")
                    .append(safe(c.getSignUpDate())).append(",")
                    .append(safe(c.getEarnedPoints())).append(",")
                    .append(safe(c.getTotalVisits())).append(",")
                    .append(safe(c.getTotalSpend())).append(",")
                    .append(safe(c.getLastPurchaseDate())).append(",")
                    .append(safe(c.isEmployee())).append(",")
                    .append(safe(c.getStartDate())).append(",")
                    .append(safe(c.getEndDate())).append(",")
                    .append(safe(c.getInternalLoyaltyCustomerId())).append("\n");
        }

        response.getWriter().write(csv.toString());
    }

    // =========================
    // CSV UPLOAD
    // =========================
    @PostMapping("/customers/upload-csv")
    public ResponseEntity<List<Customer>> uploadCSV(@RequestParam("file") MultipartFile file) {

        List<Customer> customers = new ArrayList<>();

        if (file.isEmpty())
            return ResponseEntity.badRequest().build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] columns = line.split(",", -1);

                if (columns.length < 13)
                    continue;

                try {
                    Customer c = new Customer();
                    c.setCurrentRank(columns[0]);
                    c.setDisplayId(columns[1]);
                    c.setName(columns[2]);
                    c.setPhone(columns[3]);
                    c.setEmail(columns[4]);
                    c.setSignUpDate(parseInstant(columns[5]));
                    c.setEarnedPoints(columns[6].isEmpty() ? 0 : Integer.parseInt(columns[6]));
                    c.setTotalVisits(columns[7].isEmpty() ? 0 : Integer.parseInt(columns[7]));
                    c.setTotalSpend(columns[8].isEmpty() ? 0 : Double.parseDouble(columns[8]));
                    c.setLastPurchaseDate(parseInstant(columns[9]));
                    c.setEmployee(columns[10].equalsIgnoreCase("true"));
                    c.setStartDate(parseInstant(columns[11]));
                    c.setEndDate(parseInstant(columns[12]));
                    c.setInternalLoyaltyCustomerId(columns[13]);
                    c.setPassword(passwordEncoder.encode("defaultPassword"));

                    customers.add(c);
                } catch (Exception ex) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }

            customerRepository.saveAll(customers);
            return ResponseEntity.ok(customers);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helpers =========================

    private Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return Instant.parse(dateStr);
    }

    private String safe(Object val) {
        return val == null ? "" : val.toString();
    }

    private static class ApiResponse {
        private final String message;
        private final Object data;

        public ApiResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() { return message; }

        public Object getData() { return data; }
    }
}
