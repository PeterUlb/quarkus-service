package io.ulbrich.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@ApplicationScoped
public class JwtTokenGenerator {
    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    public static int currentTimeInSecs() {
        long currentTimeMS = System.currentTimeMillis();
        return (int) (currentTimeMS / 1000);
    }

    public String generateToken() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("username=alice&password=alice&grant_type=password"))
                .header("content-type", "application/x-www-form-urlencoded")
                .header("Authorization", basicAuth("image-service", "secret"))
                .uri(URI.create(authServerUrl + "/protocol/openid-connect/token"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }

            ObjectNode node = new ObjectMapper().readValue(response.body(), ObjectNode.class);
            if (!node.has("access_token")) {
                throw new RuntimeException("Failed to get JWT");
            }

            return node.get("access_token").asText();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get JWT");
        }

        //        String userId = "1";
//        Set<String> roles = Set.of("user");
//
//        long currentTimeInSecs = currentTimeInSecs();
//        return Jwt.claims()
//                .subject(userId)
//                .issuedAt(currentTimeInSecs)
//                .expiresAt(currentTimeInSecs + 1800)
//                .groups(roles)
//                .sign();
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
