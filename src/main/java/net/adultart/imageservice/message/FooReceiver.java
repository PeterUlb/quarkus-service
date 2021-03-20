package net.adultart.imageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.service.ImageService;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class FooReceiver {

    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);
    // TODO Config Property
    private final int poolSize = 2;
    // TODO Config Property
    private final int queueSize = 50;
    @Inject
    SqsClient sqs;
    @Inject
    AwsImageConfig awsImageConfig;
    @Inject
    ImageService imageService;
    ThreadPoolExecutor executorService;

    public FooReceiver() {
        executorService = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize));
    }

    // TODO Config Property
    @Scheduled(every = "5s")
    void testReceive() {
        List<Message> messages = sqs.receiveMessage(m -> m
                .maxNumberOfMessages(Math.min(10, executorService.getQueue().remainingCapacity())) // SQS Limit is 10
                .queueUrl(awsImageConfig.getCreatedQueueUrl()))
                .messages();

        for (Message message : messages) {
            executorService.submit(() -> {
                try {
                    processMessage(message);
                } catch (JsonProcessingException e) {
                    LOGGER.error("Processing of ImageCreated message failed", e);
                }
                sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
            });
        }
    }

    private void processMessage(Message message) throws JsonProcessingException {
        LOGGER.debug(message.body());
        JsonNode json = new ObjectMapper().readTree(message.body());
        String key = json.at("/Records/0/s3/object/key").asText();
        int pos = key.lastIndexOf("/");
        if (pos != -1) {
            key = key.substring(pos + 1);
        }
        LOGGER.info(key);
        imageService.updateImageState(key, ImageStatus.UPLOADED);
    }
}
