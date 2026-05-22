package com.b9.json.jsonplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class JsonPlatformApplicationTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {});
    }

    @Test
    void testMain() {
        assertDoesNotThrow(() -> {
            JsonPlatformApplication.main(new String[] {
                    "--spring.profiles.active=test",
                    "--spring.main.web-application-type=none",
                    "--spring.datasource.url=jdbc:h2:mem:testdb_main",
                    "--spring.datasource.driver-class-name=org.h2.Driver",
                    "--spring.datasource.username=sa",
                    "--spring.datasource.password=",
                    "--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
            });
        });
    }

    @Test
    void testSetIfPresent_WithNullAndBlankValues() throws Exception {
        java.lang.reflect.Method method = JsonPlatformApplication.class.getDeclaredMethod("setIfPresent", String.class, String.class);
        method.setAccessible(true);

        method.invoke(null, "test.key.null", null);
        assertNull(System.getProperty("test.key.null"));

        method.invoke(null, "test.key.blank", "   ");
        assertNull(System.getProperty("test.key.blank"));
    }
}