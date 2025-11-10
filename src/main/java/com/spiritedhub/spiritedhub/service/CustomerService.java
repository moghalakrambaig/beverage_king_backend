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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Save customers imported from CSV file
     */
    public List<Customer> saveCustomersFromCsv(MultipartFile file) throws IOException, CsvValidationException {
        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            reader.readNext(); // skip header row

            while ((nextLine = reader.readNext()) != null) {
                try {
                    if (nextLine.length < 14) {
                        System.err.println("Skipping row due to insufficient columns: " + String.join(",", nextLine));
                        continue;
                    }

                    Customer customer = new Customer();

                    // ✅ Column order: Current Rank, Display ID, Name, Phone, Email, Sign Up Date,
                    // Earned Points, Total Visits, Total Spend, Last Purchase Date,
                    // Is Employee, Start Date, End Date, internal_loyalty_customer_id
                    customer.setId(Long.parseLong(nextLine[0]));
                    customer.setCurrentRank(nextLine[1]);
                    customer.setName(nextLine[2]);
                    customer.setPhone(nextLine[3]);
                    customer.setEmail(nextLine[4]);

                    // Sign Up Date
                    customer.setSignUpDate(parseDateSafe(nextLine[5]));

                    // Numeric fields
                    customer.setEarnedPoints(parseIntSafe(nextLine[6]));
                    customer.setTotalVisits(parseIntSafe(nextLine[7]));
                    customer.setTotalSpend(parseDoubleSafe(nextLine[8]));

                    // Last Purchase Date
                    customer.setLastPurchaseDate(parseDateSafe(nextLine[9]));

                    // Is Employee
                    customer.setEmployee(Boolean.parseBoolean(nextLine[10].trim()));

                    // Start and End Dates
                    customer.setStartDate(parseDateSafe(nextLine[11]));
                    customer.setEndDate(parseDateSafe(nextLine[12]));

                    // Internal Loyalty Customer ID
                    customer.setInternalLoyaltyCustomerId(nextLine[13]);

                    // Default encoded password
                    customer.setPassword(passwordEncoder.encode("defaultPassword"));

                    customers.add(customer);

                } catch (Exception e) {
                    System.err.println("Skipping row due to parsing error: " + String.join(",", nextLine));
                }
            }
        }

        return customerRepository.saveAll(customers);
    }

    // ======================
    // Helper Methods
    // ======================

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private LocalDate parseDateSafe(String value) {
        try {
            if (value == null || value.isEmpty()) return null;
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
