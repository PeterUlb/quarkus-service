package net.adultart.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.config.UploadProcessorConfig;
import net.adultart.imageservice.service.ImageService;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ImageUploadedReceiver {

    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    SqsClient sqs;
    @Inject
    UploadProcessorConfig uploadProcessorConfig;
    @Inject
    AwsImageConfig awsImageConfig;
    @Inject
    ImageService imageService;
    ThreadPoolExecutor executor;

    @PostConstruct
    void init() {
        executor = new ThreadPoolExecutor(uploadProcessorConfig.getPoolSize(), uploadProcessorConfig.getPoolSize(),
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(uploadProcessorConfig.getQueueSize()));
    }

    @Scheduled(every = "{upload.processor.poll-delay}")
    public void pollImageUploadedMessage() {
        while (executor.getQueue().remainingCapacity() > 0) {
            List<Message> messages = sqs.receiveMessage(m -> m
                    .maxNumberOfMessages(Math.min(10, executor.getQueue().remainingCapacity())) // SQS Limit is 10, and do not poll more than we can process
                    .waitTimeSeconds(7)
                    .queueUrl(awsImageConfig.getCreatedQueueUrl()))
                    .messages();

            LOGGER.debug(messages.size());

            if (messages.isEmpty()) {
                return;
            }

            for (Message message : messages) {
                executor.submit(() -> {
                    try {
                        processMessage(message);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Processing of ImageCreated message failed", e);
                    }
                    LOGGER.debug("Delete msg");
                    sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
                });
            }
        }
    }

    private void processMessage(Message message) throws JsonProcessingException {
        LOGGER.debug(message.body());
        JsonNode json = new ObjectMapper().readTree(message.body());
        String key = json.at("/Records/0/s3/object/key").asText();
        LOGGER.debug(key);
        imageService.processImageAfterUpload(key);
    }
}
