package net.adultart.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;

import java.io.IOException;
import java.util.Map;

public class LocalStackResource implements QuarkusTestResourceLifecycleManager {

    private final String QUEUE_NAME = "image-service-uploaded";
    private final String BUCKET = "local-test-bucket";
    private final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("DEFAULT_REGION", "eu-central-1");

    @Override
    public Map<String, String> start() {
        localStackContainer.start();

        try {
//            localStackContainer.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
            localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        SqsClient sqsClient = SqsClient
                .builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localStackContainer.getAccessKey(), localStackContainer.getSecretKey()
                )))
                .region(Region.of(localStackContainer.getRegion()))
                .build();

        //http://localhost:49728/000000000000/image-service-uploaded (localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3) + acc + queuename)
        CreateQueueResponse queueResponse = sqsClient.createQueue(builder -> builder.queueName(QUEUE_NAME));

        return Map.of(
                "%test.quarkus.s3.endpoint-override", localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
                "%test.quarkus.sqs.endpoint-override", localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString(),
                "%test.quarkus.s3.aws.region", localStackContainer.getRegion(),
                "%test.quarkus.sqs.aws.region", localStackContainer.getRegion(),
                "%test.quarkus.s3.aws.credentials.static-provider.access-key-id", localStackContainer.getAccessKey(),
                "%test.quarkus.sqs.aws.credentials.static-provider.access-key-id", localStackContainer.getAccessKey(),
                "%test.quarkus.s3.aws.credentials.static-provider.secret-access-key", localStackContainer.getSecretKey(),
                "%test.quarkus.sqs.aws.credentials.static-provider.secret-access-key", localStackContainer.getSecretKey(),
                "%test.aws.image.created-queue-url", queueResponse.queueUrl()
        );
    }

    @Override
    public void stop() {
        localStackContainer.stop();
    }
}


