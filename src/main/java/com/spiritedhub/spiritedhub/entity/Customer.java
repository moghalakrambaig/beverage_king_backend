package com.spiritedhub.spiritedhub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    // Dynamic fields stored as key-value pairs
    private Map<String, Object> dynamicFields = new HashMap<>();

    // Getters and Setters
    public Map<String, Object> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(Map<String, Object> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
