package com.b9.json.jsonplatform.auth.application.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserInternalResponseTest {

    @Test
    void testGettersAndSetters() {
        UUID mockId = UUID.randomUUID();
        UserInternalResponse response = new UserInternalResponse(mockId, "rafasya", "Rafasya Muhammad", "08123456789");

        assertEquals(mockId, response.getId());
        assertEquals("rafasya", response.getUsername());
        assertEquals("Rafasya Muhammad", response.getFullName());
        assertEquals("08123456789", response.getPhoneNumber());

        UUID newId = UUID.randomUUID();
        response.setId(newId);
        response.setUsername("bambang");
        response.setFullName("Bambang Pamungkas");
        response.setPhoneNumber("0899999");

        assertEquals(newId, response.getId());
        assertEquals("bambang", response.getUsername());
        assertEquals("Bambang Pamungkas", response.getFullName());
        assertEquals("0899999", response.getPhoneNumber());
    }
}