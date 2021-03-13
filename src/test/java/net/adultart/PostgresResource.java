package net.adultart;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    private final PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:11.1")
                    .withDatabaseName("imageservicedb")
                    .withUsername("dev")
                    .withPassword("letmein")
            ;

    @Override
    public Map<String, String> start() {
        db.start();
        return Map.of(
                "%test.quarkus.datasource.jdbc.url", db.getJdbcUrl(),
                "%test.quarkus.flyway.migrate-at-start", "true"
        );
    }

    @Override
    public void stop() {
        db.stop();
    }
}


