package com.b9.json.jsonplatform.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponse {
    private String id;
    private String email;
    private String username;
    private String role;
    private String message;
}