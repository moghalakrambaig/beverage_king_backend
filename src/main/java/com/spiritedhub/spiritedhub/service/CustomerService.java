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
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Customer> saveCustomersFromCsv(MultipartFile file) throws IOException, CsvValidationException {
        List<Customer> customers = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            reader.readNext(); // Skip header
            while ((nextLine = reader.readNext()) != null) {
                Customer customer = new Customer();
                customer.setCus_name(nextLine[0]);
                customer.setMobile(nextLine[1]);
                customer.setEmail(nextLine[2]);
                customer.setPoints(Integer.parseInt(nextLine[3]));
                customer.setPassword(passwordEncoder.encode("defaultPassword")); // Set a default password or generate one
                customers.add(customer);
            }
        }
        return customerRepository.saveAll(customers);
    }
}
