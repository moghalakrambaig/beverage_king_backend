package com.spiritedhub.spiritedhub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayId;
    private String name;
    private String phone;
    private String email;
    private LocalDate signUpDate;
    private String currentRank; 

    @Column(nullable = false)
    private int earnedPoints = 0; // default 0

    @Column(nullable = false)
    private int totalVisits = 0; // default 0

    @Column(nullable = false)
    private double totalSpend = 0.0; // default 0.0

    private LocalDate lastPurchaseDate;

    @JsonProperty("isEmployee")
    private boolean employee = false;

    private LocalDate startDate;
    private LocalDate endDate;

    // internal_loyalty_customer_id from CSV
    private String internalLoyaltyCustomerId;

    // Password for login
    private String password;

    // ======================
    // Getters and Setters
    // ======================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
    }

    public LocalDate getSignUpDate() {
        return signUpDate;
    }

    public void setSignUpDate(LocalDate signUpDate) {
        this.signUpDate = signUpDate;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(Integer earnedPoints) {
        this.earnedPoints = (earnedPoints != null) ? earnedPoints : 0;
    }

    public int getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(Integer totalVisits) {
        this.totalVisits = (totalVisits != null) ? totalVisits : 0;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(Double totalSpend) {
        this.totalSpend = (totalSpend != null) ? totalSpend : 0.0;
    }

    public LocalDate getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDate lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getInternalLoyaltyCustomerId() {
        return internalLoyaltyCustomerId;
    }

    public void setInternalLoyaltyCustomerId(String internalLoyaltyCustomerId) {
        this.internalLoyaltyCustomerId = internalLoyaltyCustomerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
