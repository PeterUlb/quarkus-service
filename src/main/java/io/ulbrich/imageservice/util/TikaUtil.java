package io.ulbrich.imageservice.util;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Optional;

@ApplicationScoped
public class TikaUtil {

    @Inject
    Parser parser;

    public Metadata extractMetadata(InputStream inputStream) throws TikaException, SAXException, IOException {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        parser.parse(inputStream, handler, metadata, context);
        return metadata;
    }

    public long getHeight(Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException();
        }

        String contentType = getContentType(metadata).orElseThrow();
        switch (contentType) {
            case "image/jpeg":
                String h = metadata.get("Image Height");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h.substring(0, h.indexOf(" ")));
            case "image/png":
            case "image/gif":
                h = metadata.get("height");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h);
            default:
                throw new IllegalArgumentException();
        }
    }

    public long getWidth(Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException();
        }

        String contentType = getContentType(metadata).orElseThrow();
        switch (contentType) {
            case "image/jpeg":
                String h = metadata.get("Image Width");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h.substring(0, h.indexOf(" ")));
            case "image/png":
            case "image/gif":
                h = metadata.get("width");
                if (h == null) {
                    throw new NoSuchElementException();
                }
                return Long.parseLong(h);
            default:
                throw new IllegalArgumentException();
        }
    }

    public Optional<String> getContentType(Metadata metadata) {
        return Optional.ofNullable(metadata.get("Content-Type"));
    }
}
