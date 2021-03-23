package net.adultart.imageservice.producer;

import io.quarkus.runtime.Startup;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;

@ApplicationScoped
public class AppProducers {
    @Produces
    @Startup
    public Parser parser() throws TikaException, IOException, SAXException {
        return new AutoDetectParser(new TikaConfig(getClass().getResource("/tika-config.xml")));
    }
}
