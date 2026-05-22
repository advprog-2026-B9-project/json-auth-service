package com.b9.json.jsonplatform.auth.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PublicProfileResponseTest {

    @Test
    void testGettersAndSetters() {
        PublicProfileResponse response = new PublicProfileResponse();

        response.setUsername("budi_jastip");
        response.setFullName("Budi Santoso");
        response.setEmail("budi@example.com");
        response.setRole("JASTIPER");
        response.setKycStatus("VERIFIED");
        response.setBanned(false);
        response.setRating(4.8);
        response.setTotalReviews(25);
        response.setTotalSuccessfulTransactions(100L);

        assertEquals("budi_jastip", response.getUsername());
        assertEquals("Budi Santoso", response.getFullName());
        assertEquals("budi@example.com", response.getEmail());
        assertEquals("JASTIPER", response.getRole());
        assertEquals("VERIFIED", response.getKycStatus());
        assertFalse(response.isBanned());
        assertEquals(4.8, response.getRating());
        assertEquals(25, response.getTotalReviews());
        assertEquals(100L, response.getTotalSuccessfulTransactions());
    }
}