package io.ulbrich.imageservice.producer.gcp;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import io.ulbrich.imageservice.config.GcpConfig;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class CredentialProducer {
    private static final Logger LOG = Logger.getLogger(CredentialProducer.class);

    @Inject
    GcpConfig gcpConfig;

    @Produces
    @Singleton
    public Credentials storage() throws IOException {
        LOG.debug("Initializing GCPCredentials");

        try (InputStream fileInputStream = Files.newInputStream(Path.of(gcpConfig.getCredentials().getLocation()))) {
            return GoogleCredentials.fromStream(fileInputStream);
        } catch (IOException e) {
            LOG.warn("Error using gcp key file: " + e.getClass().getName() + " (" + e.getMessage() + ")");
            return GoogleCredentials.getApplicationDefault();
        }
    }
}
