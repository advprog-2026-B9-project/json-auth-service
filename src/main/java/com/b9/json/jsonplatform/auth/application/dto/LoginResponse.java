package com.b9.json.jsonplatform.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String id;
    private String token;
    private String email;
    private String username;
    private String role;
}