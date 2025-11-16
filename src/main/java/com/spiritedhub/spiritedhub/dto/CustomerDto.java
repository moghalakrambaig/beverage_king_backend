package com.spiritedhub.spiritedhub.dto;

import java.time.LocalDate;

public class CustomerDto {

    private Long id;
    private String currentRank; // Database ID (Primary Key)
    private String displayId; // Display ID (for frontend or internal use)
    private String name; // Customer name
    private String phone; // Customer phone number
    private String email; // Customer email
    private LocalDate signUpDate; // Date of signup
    private int earnedPoints; // Loyalty points earned
    private int totalVisits; // Total number of visits
    private double totalSpend; // Total money spent
    private LocalDate lastPurchaseDate; // Last purchase date
    private boolean isEmployee; // Employee flag
    private LocalDate startDate; // Start date (for employees or program start)
    private LocalDate endDate; // End date (if applicable)
    private String internalLoyaltyCustomerId; // Internal system reference

    // ==========================
    // Constructors
    // ==========================

    public CustomerDto() {
    }

    public CustomerDto(Long id, String currentRank, String displayId, String name, String phone, String email,
            LocalDate signUpDate, int earnedPoints, int totalVisits, double totalSpend,
            LocalDate lastPurchaseDate, boolean isEmployee, LocalDate startDate,
            LocalDate endDate, String internalLoyaltyCustomerId) {
        this.id = id;
        this.currentRank = currentRank;
        this.displayId = displayId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.signUpDate = signUpDate;
        this.earnedPoints = earnedPoints;
        this.totalVisits = totalVisits;
        this.totalSpend = totalSpend;
        this.lastPurchaseDate = lastPurchaseDate;
        this.isEmployee = isEmployee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.internalLoyaltyCustomerId = internalLoyaltyCustomerId;
    }

    // ==========================
    // Getters and Setters
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
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

    public LocalDate getSignUpDate() {
        return signUpDate;
    }

    public void setSignUpDate(LocalDate signUpDate) {
        this.signUpDate = signUpDate;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public int getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(int totalVisits) {
        this.totalVisits = totalVisits;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(double totalSpend) {
        this.totalSpend = totalSpend;
    }

    public LocalDate getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDate lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setEmployee(boolean employee) {
        isEmployee = employee;
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
}
