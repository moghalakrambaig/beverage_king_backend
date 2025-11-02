package com.spiritedhub.spiritedhub.dto;

public class CustomerDto {

    private Long id;
    private String cus_name;
    private String mobile;
    private String email;
    private int points;

    public CustomerDto(Long id, String cus_name, String mobile, String email, int points) {
        this.id = id;
        this.cus_name = cus_name;
        this.mobile = mobile;
        this.email = email;
        this.points = points;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCus_name() {
        return cus_name;
    }

    public void setCus_name(String cus_name) {
        this.cus_name = cus_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
