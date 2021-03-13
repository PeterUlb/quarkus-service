package net.adultart.imageservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadInfoDto;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.service.ImageService;
import org.apache.commons.lang3.StringUtils;
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
    S3Presigner s3Presigner;

    @Inject
    AwsImageConfig awsImageConfig;

    @Inject
    ImageService imageService;

    @POST
    @Path("/signed")
    @Authenticated
    public ImageUploadInfoDto signed(@Valid ImageUploadRequestDto imageUploadRequestDto) {
        LOGGER.info(jwt.getSubject());

        String externalKey = imageService.createImageEntry(imageUploadRequestDto.getFileName(), imageUploadRequestDto.getBytes());

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(popr -> popr
                .putObjectRequest(por -> por
                                .bucket(awsImageConfig.getBucket())
                                .key("uploads/" + externalKey)
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
    void testReceive() {
        List<Message> messages = sqs.receiveMessage(m -> m
                .maxNumberOfMessages(10)
                .queueUrl(awsImageConfig.getCreatedQueueUrl()))
                .messages();

        for (Message message : messages) {
            try {
                processMessage(message);
            } catch (JsonProcessingException e) {
                // In this case, the message is not processable, delete from queue
                sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
                continue;
            }
            sqs.deleteMessage(m -> m.queueUrl(awsImageConfig.getCreatedQueueUrl()).receiptHandle(message.receiptHandle()));
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