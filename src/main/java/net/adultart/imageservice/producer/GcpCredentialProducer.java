package net.adultart.imageservice.producer;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.arc.profile.UnlessBuildProfile;
import net.adultart.imageservice.config.GcpConfig;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class GcpCredentialProducer {

    private static final Logger LOG = Logger.getLogger(GcpCredentialProducer.class);

    @Inject
    GcpConfig gcpConfig;

    @Produces
    @Singleton
    @UnlessBuildProfile("test")
    public Credentials credentials() throws IOException {
        LOG.debug("Initializing Google Credentials");

        try (InputStream fileInputStream = Files.newInputStream(Path.of(gcpConfig.getCredentials().getLocation()))) {
            return GoogleCredentials.fromStream(fileInputStream);
        } catch (IOException e) {
            LOG.warn("Error using gcp key file: " + e.getClass().getName() + " (" + e.getMessage() + ")");
            return GoogleCredentials.getApplicationDefault();
        }
    }

    @Produces
    @Singleton
    @IfBuildProfile("test")
    public Credentials credentialsTest() throws IOException, URISyntaxException {
        LOG.debug("Initializing Google Credentials Test");

        try (InputStream fileInputStream = Files.newInputStream(Path.of(ClassLoader.getSystemResource("gcpTestKey.json").toURI()))) {
            return GoogleCredentials.fromStream(fileInputStream);
        }
    }
}
