package io.ulbrich.imageservice.producer;

import io.quarkus.runtime.Startup;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;

@ApplicationScoped
public class AppProducers {
    private static final Logger LOG = Logger.getLogger(AppProducers.class);

    @Produces
    @ApplicationScoped
    @Startup
    public Parser parser() throws TikaException, IOException, SAXException {
        LOG.debug("Creating Parser");
        return new AutoDetectParser(new TikaConfig(getClass().getResource("/tika-config.xml")));
    }
}
