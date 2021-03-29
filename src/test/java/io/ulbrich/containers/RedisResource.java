package io.ulbrich.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class RedisResource implements QuarkusTestResourceLifecycleManager {
    private final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);

    @Override
    public Map<String, String> start() {
        redis.start();
        return Map.of(
                "%test.quarkus.redis.hosts", String.format("redis://%s:%s", redis.getHost(), redis.getFirstMappedPort())
        );
    }

    @Override
    public void stop() {
        redis.stop();
    }
}


