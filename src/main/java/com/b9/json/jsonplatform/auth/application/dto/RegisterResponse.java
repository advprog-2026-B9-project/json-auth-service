package com.b9.json.jsonplatform.auth.application.dto;

public class RegisterResponse {
    private String id;
    private String email;
    private String username;
    private String role;
    private String message;

    public RegisterResponse(String id, String email, String username, String role, String message) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}