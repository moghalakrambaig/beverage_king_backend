package com.spiritedhub.spiritedhub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;  // MongoDB uses String IDs

    private String currentRank;
    private String displayId;
    private String name;
    private String phone;
    private String email;

    private Instant signUpDate;
    private int earnedPoints = 0;
    private int totalVisits = 0;
    private double totalSpend = 0.0;

    private Instant lastPurchaseDate;

    @JsonProperty("isEmployee")
    private boolean employee = false;

    private Instant startDate;
    private Instant endDate;

    private String internalLoyaltyCustomerId;

    // For password reset
    private String password;
    private String resetPasswordToken;
    private Instant resetPasswordExpiry;

    // 🔥 DYNAMIC FIELDS for CSV upload
    private Map<String, Object> dynamicFields;

    // ======================
    // Getters and Setters
    // ======================

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Instant getSignUpDate() {
        return signUpDate;
    }

    public void setSignUpDate(Instant signUpDate) {
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

    public Instant getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(Instant lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean employee) {
        this.employee = employee;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
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

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public Instant getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }

    public void setResetPasswordExpiry(Instant resetPasswordExpiry) {
        this.resetPasswordExpiry = resetPasswordExpiry;
    }

    public Map<String, Object> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(Map<String, Object> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }
}
