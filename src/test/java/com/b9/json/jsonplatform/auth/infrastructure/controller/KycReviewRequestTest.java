package com.b9.json.jsonplatform.auth.infrastructure.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KycReviewRequestTest {

    @Test
    void testGettersAndSetters() {
        KycReviewRequest request = new KycReviewRequest();
        request.setEmail("jastiper@example.com");
        request.setApproved(true);

        assertEquals("jastiper@example.com", request.getEmail());
        assertTrue(request.isApproved());
    }
}