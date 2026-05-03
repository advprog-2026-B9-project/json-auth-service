package com.b9.json.jsonplatform.auth.domain;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmail("mahasiswa@ui.ac.id");
        user.setPassword("rahasia123");
        user.setUsername("mahasiswa_ui");

        assertEquals(id, user.getId());
        assertEquals("mahasiswa@ui.ac.id", user.getEmail());
        assertEquals("rahasia123", user.getPassword());
        assertEquals("mahasiswa_ui", user.getUsername());
        assertEquals(UserRole.TITIPERS, user.getRole());
    }

    @Test
    void testUserCustomRole() {
        User user = new User();
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }
}