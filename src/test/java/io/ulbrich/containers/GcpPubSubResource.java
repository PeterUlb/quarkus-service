package io.ulbrich.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class GcpPubSubResource implements QuarkusTestResourceLifecycleManager {
    private final PubSubEmulatorContainer pubSub = new PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators")
    );

    @Override
    public Map<String, String> start() {
        pubSub.start();
        return Map.of(
                "%test.gcp.pub-sub.endpoint-override", pubSub.getEmulatorEndpoint(),
                "%test.upload.processor.subscription-name", "image-uploaded"
        );
    }

    @Override
    public void stop() {
        pubSub.stop();
    }
}


