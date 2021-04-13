package io.ulbrich.mock;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@QuarkusTestResource(value = MockContainers.class, parallel = true)
public class MockContainers implements QuarkusTestResourceLifecycleManager {
    public static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("src\\main\\docker\\mock\\compose-dev.yml"))
                    .withLocalCompose(true)
                    .withExposedService("redis_1", 6379)
                    .withExposedService("country-service-mock_1", 8080)
                    .withExposedService("fake-gcs-server_1", 8080)
                    .withExposedService("postgres_1", 5432)
                    .withExposedService("gcp-pubsub-emulator_1", 8085);

    String subscriptionId = "img-upload-test-sub";
    String mockProjectId = "image-service-test";

    @Override
    public Map<String, String> start() {
        environment.start();

        String redisUrl = String.format("redis://%s:%s",
                environment.getServiceHost("redis_1", 6379),
                environment.getServicePort("redis_1", 6379));
        String countryServiceUrl = String.format("http://%s:%s",
                environment.getServiceHost("country-service-mock_1", 8080),
                environment.getServicePort("country-service-mock_1", 8080));

        String gcsUrl = String.format("http://%s:%s",
                environment.getServiceHost("fake-gcs-server_1", 8080),
                environment.getServicePort("fake-gcs-server_1", 8080));

        String postgresUrl = String.format("jdbc:postgresql://%s:%s/imageservicedb",
                environment.getServiceHost("postgres_1", 5432),
                environment.getServicePort("postgres_1", 5432));

        String pubSubEndpoint = String.format("%s:%s",
                environment.getServiceHost("gcp-pubsub-emulator_1", 8085),
                environment.getServicePort("gcp-pubsub-emulator_1", 8085));

        initPubSub(pubSubEndpoint);

        return Map.of(
                "%test.quarkus.redis.hosts", redisUrl,
                "%test.country-api/mp-rest/url", countryServiceUrl,
                "%test.gcp.cloud-storage.endpoint-override", gcsUrl,
                "%test.quarkus.datasource.jdbc.url", postgresUrl,
                "%test.quarkus.flyway.migrate-at-start", "true",
                "%test.quarkus.datasource.username", "dev",
                "%test.quarkus.datasource.password", "letmein",
                "%test.gcp.project-id", mockProjectId,
                "%test.gcp.pub-sub.endpoint-override", pubSubEndpoint,
                "%test.upload.processor.subscription-name", subscriptionId
        );
    }

    @Override
    public void stop() {
        environment.stop();
    }

    private void initPubSub(String pubSubEndpoint) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(pubSubEndpoint).usePlaintext().build();
        String subscriptionId = "img-upload-test-sub";
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            NoCredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            String topicId = "img-upload-test";
            createTopic(topicId, channelProvider, credentialsProvider);

            createSubscription(subscriptionId, topicId, channelProvider, credentialsProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }
    }

    private void createTopic(String topicId, TransportChannelProvider channelProvider, NoCredentialsProvider credentialsProvider) throws IOException {
        TopicAdminSettings topicAdminSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
            TopicName topicName = TopicName.of(mockProjectId, topicId);
            topicAdminClient.createTopic(topicName);
        }
    }

    private void createSubscription(String subscriptionId, String topicId, TransportChannelProvider channelProvider, NoCredentialsProvider credentialsProvider) throws IOException {
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings);
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(mockProjectId, subscriptionId);
        subscriptionAdminClient.createSubscription(subscriptionName, TopicName.of(mockProjectId, topicId), PushConfig.getDefaultInstance(), 10);
    }
}
