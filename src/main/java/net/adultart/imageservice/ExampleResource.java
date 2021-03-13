package net.adultart.imageservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadInfoDto;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class ExampleResource {

    private static final Logger LOGGER = Logger.getLogger(ExampleResource.class);

    @Inject
    SqsClient sqs;

    @Inject
    S3Client s3;

    @Inject
    S3Presigner s3Presigner;

    @Inject
    AwsImageConfig awsImageConfig;

    @GET
    public Response hello() {
        SendMessageResponse response = sqs.sendMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).messageBody("Rudy"));
        return Response.accepted("Success").build();
    }

    @GET
    @Path("/receive")
    public List<String> receive() {
        List<Message> messages = sqs.receiveMessage(m -> m.maxNumberOfMessages(10).queueUrl(awsImageConfig.getCreatedQueueUrl())).messages();

        for (Message message : messages) {
            sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
        }

        return messages.stream()
                .map(Message::body)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/signed")
    public ImageUploadInfoDto signed() {
        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(popr -> popr
                .putObjectRequest(por -> por
                                .bucket(awsImageConfig.getBucket())
                                .key("uploads/test.png")
//                        .acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                                .metadata(Map.of("owner", "1234"))
                                .contentLength(52926L)
                )
                .signatureDuration(Duration.ofMinutes(20)));


//        LOGGER.info("Pre-signed URL to upload a file to: " +
//                presignedPutObjectRequest.url());
//        LOGGER.info("Which HTTP method needs to be used when uploading a file: " +
//                presignedPutObjectRequest.httpRequest().method());
//        LOGGER.info("Which headers need to be sent with the upload: " +
//                presignedPutObjectRequest.signedHeaders());
        return new ImageUploadInfoDto(presignedPutObjectRequest.url().toExternalForm(),
                presignedPutObjectRequest.httpRequest().method().name(),
                presignedPutObjectRequest.signedHeaders());
    }

    @Scheduled(every = "2s")
    void testReceive() throws JsonProcessingException {
        List<Message> messages = sqs.receiveMessage(m -> m
                .maxNumberOfMessages(10)
                .queueUrl(awsImageConfig.getCreatedQueueUrl()))
                .messages();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Message message : messages) {
            LOGGER.debug(message.body());

            JsonNode json = objectMapper.readTree(message.body());
            String key = json.at("/Records/0/s3/object/key").asText();
            LOGGER.info(key);


            HeadObjectResponse headObjectResponse = s3.headObject(builder -> builder.bucket(awsImageConfig.getBucket()).key(key));
            LOGGER.info(headObjectResponse.metadata());
            sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
        }
    }
}