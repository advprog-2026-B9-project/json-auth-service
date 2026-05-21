package com.b9.json.jsonplatform.auth.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String address;
}