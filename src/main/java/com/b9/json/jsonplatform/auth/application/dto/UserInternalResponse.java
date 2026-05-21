package com.b9.json.jsonplatform.auth.application.dto;

public class UserInternalResponse {
    private String username;
    private String fullName;
    private String phoneNumber;

    public UserInternalResponse(String username, String fullName, String phoneNumber) {
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}