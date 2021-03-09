package net.adultart.imageservice;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
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

    @GET
    public Response hello() {
        SendMessageResponse response = sqs.sendMessage(m -> m.queueUrl("https://sqs.eu-central-1.amazonaws.com/621918013978/TestQueue").messageBody("Rudy"));
        return Response.accepted("Success").build();
    }

    @GET
    @Path("/receive")
    public List<String> receive() {
        List<Message> messages = sqs.receiveMessage(m -> m.maxNumberOfMessages(10).queueUrl("https://sqs.eu-central-1.amazonaws.com/621918013978/TestQueue")).messages();

        for (Message message : messages) {
            sqs.deleteMessage(m -> m.queueUrl("https://sqs.eu-central-1.amazonaws.com/621918013978/TestQueue").receiptHandle(message.receiptHandle()));
        }

        return messages.stream()
                .map(Message::body)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/signed")
    public String signed() {
        System.out.println(s3Presigner);
        return "";
    }
}