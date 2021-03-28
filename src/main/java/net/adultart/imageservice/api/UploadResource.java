package net.adultart.imageservice.api;

import io.quarkus.security.Authenticated;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadInfoDto;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.interceptor.RateLimited;
import net.adultart.imageservice.model.ImagePrivacy;
import net.adultart.imageservice.service.ImageService;
import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Authenticated
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
    @RateLimited(group = 1, maxRequests = 20)
    public Response signed(@Valid ImageUploadRequestDto imageUploadRequestDto) {
        String externalKey = imageService.createImageEntry(imageUploadRequestDto, Long.parseLong(jwt.getSubject()));

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(popr -> popr
                .putObjectRequest(por -> {
                    por
                            .bucket(awsImageConfig.getBucket())
                            .key("images/" + externalKey)
                            // .acl(ObjectCannedACL.PUBLIC_READ)
                            .contentType(ContentType.IMAGE_PNG.getMimeType())
                            .metadata(Map.of("owner", jwt.getSubject()))
                            .contentLength(imageUploadRequestDto.getSize());
                    if (imageUploadRequestDto.getPrivacy().equals(ImagePrivacy.PUBLIC)) {
                        por.acl(ObjectCannedACL.PUBLIC_READ);
                    }
                })
                .signatureDuration(Duration.ofMinutes(20)));
        return Response.ok(new ImageUploadInfoDto(presignedPutObjectRequest.url().toExternalForm(),
                presignedPutObjectRequest.httpRequest().method().name(),
                presignedPutObjectRequest.signedHeaders())).build();
    }

    @GET
    @Path("/tag/{tag}")
    public Response getImagesByTag(@PathParam("tag") String tag) {
        Set<String> signedUrls = imageService.getSignedUrlsByTag(tag);
        if (signedUrls.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(signedUrls).build();
    }

    @GET
    @Path("/test")
    @PermitAll
    @RateLimited(group = 9998, maxRequests = 5)
    public Response testRateLimit() {
        return Response.ok().build();
    }
}