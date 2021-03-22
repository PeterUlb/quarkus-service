package net.adultart.imageservice.api;

import io.quarkus.security.Authenticated;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadInfoDto;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.service.ImageService;
import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.Map;

@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UploadResource {

    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    JsonWebToken jwt;

    @Inject
    S3Presigner s3Presigner;

    @Inject
    AwsImageConfig awsImageConfig;

    @Inject
    ImageService imageService;

    @POST
    @Path("/request")
    @Authenticated
    public ImageUploadInfoDto signed(@Valid ImageUploadRequestDto imageUploadRequestDto) {
        String externalKey = imageService.createImageEntry(imageUploadRequestDto, Long.parseLong(jwt.getSubject()));

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(popr -> popr
                .putObjectRequest(por -> por
                                .bucket(awsImageConfig.getBucket())
                                .key("uploads/" + externalKey)
//                        .acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                                .contentType(ContentType.IMAGE_PNG.getMimeType())
                                .metadata(Map.of("owner", jwt.getSubject()))
                                .contentLength(imageUploadRequestDto.getSize())
                )
                .signatureDuration(Duration.ofMinutes(20)));
        return new ImageUploadInfoDto(presignedPutObjectRequest.url().toExternalForm(),
                presignedPutObjectRequest.httpRequest().method().name(),
                presignedPutObjectRequest.signedHeaders());
    }
}