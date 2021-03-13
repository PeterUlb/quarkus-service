package net.adultart.imageservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadInfoDto;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UploadResource {

    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    JsonWebToken jwt;

    @Inject
    SqsClient sqs;

    @Inject
    S3Client s3;

    @Inject
    S3Presigner s3Presigner;

    @Inject
    AwsImageConfig awsImageConfig;

    @POST
    @Path("/signed")
    @Authenticated
    public ImageUploadInfoDto signed(@Valid ImageUploadRequestDto imageUploadRequestDto) {
        LOGGER.warn(jwt.getSubject());

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(popr -> popr
                .putObjectRequest(por -> por
                                .bucket(awsImageConfig.getBucket())
                                .key("uploads/" + imageUploadRequestDto.getFileName())
//                        .acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                                .contentType(ContentType.IMAGE_PNG.getMimeType())
                                .metadata(Map.of("owner", jwt.getSubject()))
                                .contentLength(imageUploadRequestDto.getBytes())
                )
                .signatureDuration(Duration.ofMinutes(20)));
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