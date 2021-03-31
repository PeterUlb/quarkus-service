package io.ulbrich.integration;

import com.google.api.core.ApiFuture;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.Credentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.ulbrich.imageservice.config.GcpConfig;
import io.ulbrich.imageservice.service.ImageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@QuarkusTest
public class ImageConfirmTest {
    @Inject
    GcpConfig gcpConfig;

    @Inject
    Credentials credentials;

    @InjectMock
    ImageService imageService;

    @Test
    public void testPubSubConfirm() throws IOException, ExecutionException, InterruptedException {
        Mockito.doNothing().when(imageService).processImageAfterUpload(Mockito.isA(String.class));

        ManagedChannel channel = ManagedChannelBuilder.forTarget(gcpConfig.getPubSub().getEndpointOverride().orElse(null)).usePlaintext().build();
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

            Publisher publisher =
                    Publisher.newBuilder(TopicName.newBuilder().setProject(gcpConfig.getProjectId()).setTopic("img-upload-test").build())
                            .setChannelProvider(channelProvider)
                            .setCredentialsProvider(() -> credentials)
                            .build();
            String msg = "{\"name\":\"images/BVXErLFEgv\",\"bucket\":\"some-bucket\"}";
            PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(msg)).build();
            ApiFuture<String> apiFuture = publisher.publish(message);
            apiFuture.get();
        } finally {
            channel.shutdown();
        }

        Mockito.verify(imageService, Mockito.timeout(1000).atLeastOnce()).processImageAfterUpload(Mockito.any());
    }
}
