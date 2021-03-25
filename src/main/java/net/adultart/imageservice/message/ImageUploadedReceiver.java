package net.adultart.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.config.UploadProcessorConfig;
import net.adultart.imageservice.service.ImageService;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class ImageUploadedReceiver {

    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);
    private final ScheduledExecutorService pollScheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "sqs-poll"));
    private final AtomicLong atomicLong = new AtomicLong();
    @Inject
    SqsClient sqs;
    @Inject
    UploadProcessorConfig uploadProcessorConfig;
    @Inject
    AwsImageConfig awsImageConfig;
    @Inject
    ImageService imageService;
    ThreadPoolExecutor executor;

    void onStart(@Observes StartupEvent ev) {
        executor = new ThreadPoolExecutor(
                uploadProcessorConfig.getPoolSize(),
                uploadProcessorConfig.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(uploadProcessorConfig.getQueueSize()),
                r -> new Thread(r, "sqs-worker-" + atomicLong.getAndIncrement()));

        // By default, this will long poll for 20 seconds (getLongPollingWait), and long poll again after 1 second (getPollDelay)
        pollScheduler.scheduleWithFixedDelay(this::pollImageUploadedMessage, 1, uploadProcessorConfig.getPollDelay(), TimeUnit.SECONDS);
    }

    void onStop(@Observes ShutdownEvent ev) {
        pollScheduler.shutdown();
        executor.shutdown();
    }

    public void pollImageUploadedMessage() {
        LOGGER.debug("Polling");
        while (executor.getQueue().remainingCapacity() > 0) { //Best guess on how many to pull. Note: If there are multiple poller threads, this might not be reliable
            List<Message> messages = sqs.receiveMessage(m -> m
                    .maxNumberOfMessages(Math.min(10, executor.getQueue().remainingCapacity())) // SQS Limit is 10, and do not poll more than we can process
                    .waitTimeSeconds(uploadProcessorConfig.getLongPollingWait())
                    .queueUrl(awsImageConfig.getCreatedQueueUrl()))
                    .messages();

            LOGGER.debug(messages.size());

            if (messages.isEmpty()) {
                return;
            }

            for (Message message : messages) {
                try {
                    executor.submit(() -> {
                        try {
                            processMessage(message);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("Processing of ImageCreated message failed", e);
                        }
                        LOGGER.debug("Delete msg");
                        sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
                    });
                } catch (RejectedExecutionException ex) {
                    // In this case, do NACK the message to be processed by someone else
                    sqs.changeMessageVisibility(builder -> builder.queueUrl(awsImageConfig
                            .getCreatedQueueUrl())
                            .receiptHandle(message.receiptHandle())
                            .visibilityTimeout(0));
                }
            }
        }
    }

    //TODO: Move to service
    private void processMessage(Message message) throws JsonProcessingException {
        LOGGER.debug(message.body());
        JsonNode json = new ObjectMapper().readTree(message.body());
        String key = json.at("/Records/0/s3/object/key").asText();
        LOGGER.debug(key);
        imageService.processImageAfterUpload(key);
    }
}
