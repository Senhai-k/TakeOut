package com.takeout;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:takeout-migration;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "takeout.seed.enabled=false"
})
@ActiveProfiles("test")
class DatabaseMigrationSmokeTests {
    @Test
    void flywayCreatesSchemaThatMatchesJpaEntities() {
    }
}
