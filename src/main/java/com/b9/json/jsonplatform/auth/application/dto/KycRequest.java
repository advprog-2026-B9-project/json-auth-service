package com.b9.json.jsonplatform.auth.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class KycRequest {
    private String email;
    private String fullName;
    private String nikKtp;
    private String ktpImageUrl;
}