package com.b9.json.jsonplatform.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserInternalResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String phoneNumber;
}