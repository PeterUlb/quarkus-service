package io.ulbrich.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class GcpCloudStorageResource implements QuarkusTestResourceLifecycleManager {
    private final GenericContainer<?> cloudStorage = new GenericContainer<>(DockerImageName.parse("fsouza/fake-gcs-server"))
            .withExposedPorts(4443)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withEntrypoint("/bin/fake-gcs-server", "-scheme", "http", "-backend", "memory");
            });

    @Override
    public Map<String, String> start() {
        cloudStorage.start();
        return Map.of(
                "%test.gcp.cloud-storage.endpoint-override", String.format("http://%s:%s", cloudStorage.getHost(), cloudStorage.getFirstMappedPort())
        );
    }

    @Override
    public void stop() {
        cloudStorage.stop();
    }
}


