package io.ulbrich.imageservice.producer.gcp;

import com.google.auth.Credentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.quarkus.runtime.Startup;
import io.ulbrich.imageservice.config.GcpConfig;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@ApplicationScoped
public class StorageProducer {
    private static final Logger LOG = Logger.getLogger(StorageProducer.class);

    @Inject
    Credentials credentials;

    @Inject
    GcpConfig gcpConfig;

    @Produces
    @Singleton
    @Startup
    public Storage storage() {
        LOG.debug("Initializing GCPStorage with " + credentials.getClass().getName());
        return StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(gcpConfig.getProjectId())
                .setHost(gcpConfig.getCloudStorage().getEndpointOverride().orElse(null))
                .build()
                .getService();
    }
}
