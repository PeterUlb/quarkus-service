package io.ulbrich.imageservice.repository;

import io.ulbrich.imageservice.model.Image;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ImageRepositoryImpl implements ImageRepository {
    @Override
    public Optional<Image> findByExternalId(String externalKey) {
        return find("externalKey", externalKey).firstResultOptional();
    }
}
