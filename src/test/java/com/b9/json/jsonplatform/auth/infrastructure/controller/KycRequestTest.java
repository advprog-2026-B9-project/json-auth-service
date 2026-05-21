package com.b9.json.jsonplatform.auth.infrastructure.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KycRequestTest {

    @Test
    void testGettersAndSetters() {
        KycRequest request = new KycRequest();
        request.setEmail("jastiper@example.com");
        request.setFullName("Budi Santoso");
        request.setNikKtp("3201012345678901");
        request.setKtpImageUrl("https://image.com/ktp.jpg");

        assertEquals("jastiper@example.com", request.getEmail());
        assertEquals("Budi Santoso", request.getFullName());
        assertEquals("3201012345678901", request.getNikKtp());
        assertEquals("https://image.com/ktp.jpg", request.getKtpImageUrl());
    }
}