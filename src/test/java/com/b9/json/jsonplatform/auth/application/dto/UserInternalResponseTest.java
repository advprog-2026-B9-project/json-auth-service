package com.b9.json.jsonplatform.auth.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserInternalResponseTest {

    @Test
    void testGettersAndSetters() {
        UserInternalResponse response = new UserInternalResponse("rafasya", "Rafasya Muhammad", "08123456789");

        assertEquals("rafasya", response.getUsername());
        assertEquals("Rafasya Muhammad", response.getFullName());
        assertEquals("08123456789", response.getPhoneNumber());

        response.setUsername("bambang");
        response.setFullName("Bambang Pamungkas");
        response.setPhoneNumber("0899999");

        assertEquals("bambang", response.getUsername());
        assertEquals("Bambang Pamungkas", response.getFullName());
        assertEquals("0899999", response.getPhoneNumber());
    }
}