package net.adultart.imageservice.producer;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class AwsProducers {
    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;
    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String accessKeyId;
    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String secretAccessKey;
    private S3Presigner s3Presigner;

    @Produces
    @ApplicationScoped
    @Startup
    public S3Presigner s3Presigner() {
        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
        return s3Presigner;
    }

    @PreDestroy
    public void destroy() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}
