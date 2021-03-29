package net.adultart.imageservice.producer;

import com.google.auth.Credentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.quarkus.runtime.Startup;
import net.adultart.imageservice.config.GcpConfig;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class AppProducers {
    private static final Logger LOG = Logger.getLogger(AppProducers.class);

    @Inject
    Credentials credentials;

    @Inject
    GcpConfig gcpConfig;

    @Produces
    @ApplicationScoped
    @Startup
    public Parser parser() throws TikaException, IOException, SAXException {
        LOG.debug("Creating Parser");
        return new AutoDetectParser(new TikaConfig(getClass().getResource("/tika-config.xml")));
    }

    @Produces
    @ApplicationScoped
    @Startup
    public Storage storage() {
        LOG.debug("Creating Storage");
        StorageOptions.Builder builder = StorageOptions.newBuilder().setCredentials(credentials);
        gcpConfig.getCloudStorage().getEndpointOverride().ifPresent(builder::setHost);

        return builder.build().getService();
    }
}
