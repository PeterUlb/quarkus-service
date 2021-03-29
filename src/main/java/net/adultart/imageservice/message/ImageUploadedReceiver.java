package net.adultart.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.Credentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.GcpConfig;
import net.adultart.imageservice.config.UploadProcessorConfig;
import net.adultart.imageservice.service.ImageService;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class ImageUploadedReceiver implements MessageReceiver {

    private static final Logger LOG = Logger.getLogger(UploadResource.class);

    @Inject
    Credentials credentials;

    @Inject
    GcpConfig gcpConfig;

    @Inject
    ImageService imageService;

    @Inject
    UploadProcessorConfig uploadProcessorConfig;

    Subscriber subscriber;

    void onStart(@Observes StartupEvent ev) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(gcpConfig.getProjectId(), uploadProcessorConfig.getSubscriptionName());

        FlowControlSettings flowControlSettings =
                FlowControlSettings.newBuilder()
                        .setMaxOutstandingElementCount(uploadProcessorConfig.getQueueSize())
                        .build();

        ExecutorProvider executorProvider =
                InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(uploadProcessorConfig.getPoolSize()).build();

        Subscriber.Builder builder = Subscriber.newBuilder(subscriptionName, this)
                .setCredentialsProvider(() -> credentials)
                .setFlowControlSettings(flowControlSettings)
                .setExecutorProvider(executorProvider);

        gcpConfig.getPubSub().getEndpointOverride().ifPresent(s -> {
            if (!s.isBlank()) {
                builder.setEndpoint(s);
            }
        });

        subscriber = builder.build();
        subscriber.startAsync().awaitRunning();
    }

    void onStop(@Observes ShutdownEvent ev) {
        subscriber.stopAsync();
    }


    //TODO: Move to service
    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        LOG.debug(message.getData().toStringUtf8());
        JsonNode json = null;
        try {
            json = new ObjectMapper().readTree(message.getData().toStringUtf8());
            String key = json.at("/name").asText();
            LOG.debug(key);
            imageService.processImageAfterUpload(key);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            consumer.ack();
        }
    }
}
