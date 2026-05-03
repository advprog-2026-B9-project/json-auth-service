package com.b9.json.jsonplatform.auth.application.dto;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponseDto {
    private final UUID id;
    private final String email;
    private final String username;
    private final String fullName;
    private final String phoneNumber;
    private final String address;
    private final UserRole role;
    private final KycStatus kycStatus;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
        this.role = user.getRole();
        this.kycStatus = user.getKycStatus();
    }
}