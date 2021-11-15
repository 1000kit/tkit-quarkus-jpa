package org.tkit.quarkus.jpa.test;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.Map;

public class PostgreSQLResource implements QuarkusTestResourceLifecycleManager {

    PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:12")
            .withUrlParam("sslmode", "disable")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres");


    @Override
    public Map<String, String> start() {
        db.start();
        return Collections.singletonMap("quarkus.datasource.jdbc.url", db.getJdbcUrl());
    }

    @Override
    public void stop() {
        db.stop();
    }
}