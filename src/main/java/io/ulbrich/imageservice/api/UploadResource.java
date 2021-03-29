package io.ulbrich.imageservice.api;

import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.*;
import io.quarkus.security.Authenticated;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.interceptor.RateLimited;
import io.ulbrich.imageservice.model.ImagePrivacy;
import io.ulbrich.imageservice.config.UploadProcessorConfig;
import io.ulbrich.imageservice.dto.ImageUploadInfoDto;
import io.ulbrich.imageservice.service.ImageService;
import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    ImageService imageService;

    @Inject
    Storage storage;

    @Inject
    UploadProcessorConfig uploadProcessorConfig;

    @POST
    @Path("/request")
    @RateLimited(group = 1, maxRequests = 20)
    public Response signed(@Valid ImageUploadRequestDto imageUploadRequestDto) {
        String externalKey = imageService.createImageEntry(imageUploadRequestDto, Long.parseLong(jwt.getSubject()));

        List<Acl> aclList;
        if (imageUploadRequestDto.getPrivacy().equals(ImagePrivacy.PUBLIC)) {
            aclList = List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        } else {
            aclList = Collections.emptyList();
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(uploadProcessorConfig.getBucket(), "images/" + externalKey))
                .setContentType(ContentType.IMAGE_PNG.getMimeType())
                .setMetadata(Map.of("owner", jwt.getSubject()))
                .setAcl(aclList)
                .build();
        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("x-goog-content-length-range", "1," + imageUploadRequestDto.getSize());
        extensionHeaders.put("Content-Type", "image/png");

        URL url =
                storage.signUrl(
                        blobInfo,
                        15,
                        TimeUnit.MINUTES,
                        Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                        Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                        Storage.SignUrlOption.withV4Signature());

        return Response.ok(new ImageUploadInfoDto(url.toExternalForm(),
                HttpMethod.PUT.name(),
                Map.of("x-goog-content-length-range", "1," + imageUploadRequestDto.getSize())
        )).build();
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