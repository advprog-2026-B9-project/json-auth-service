package com.b9.json.jsonplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JsonPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testMain() {
        JsonPlatformApplication.main(new String[] {
                "--spring.profiles.active=test",
                "--spring.main.web-application-type=none",
                "--spring.datasource.url=jdbc:h2:mem:testdb_main",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        });
    }
}