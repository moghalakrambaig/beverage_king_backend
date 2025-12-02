package com.spiritedhub.spiritedhub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    private String email;

    // Store all CSV data dynamically
    private Map<String, Object> dynamicFields;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(Map<String, Object> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
