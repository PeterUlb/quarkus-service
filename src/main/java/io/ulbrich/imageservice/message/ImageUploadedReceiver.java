package io.ulbrich.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.Credentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.PubsubMessage;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.ulbrich.imageservice.api.UploadResource;
import io.ulbrich.imageservice.config.GcpConfig;
import io.ulbrich.imageservice.config.UploadProcessorConfig;
import io.ulbrich.imageservice.service.ImageService;
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

    @Inject
    PubSubFactory pubSubFactory;

    Subscriber subscriber;

    void onStart(@Observes StartupEvent ev) {
        subscriber = pubSubFactory.createSubscriber(uploadProcessorConfig.getSubscriptionName(),
                uploadProcessorConfig.getQueueSize(), uploadProcessorConfig.getPoolSize(), this);
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
