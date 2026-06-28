package com.b9.json.jsonplatform.auth.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    private String fullName;
    private String username;
    private String phoneNumber;
    private String address;
    private String profileImageUrl;
}