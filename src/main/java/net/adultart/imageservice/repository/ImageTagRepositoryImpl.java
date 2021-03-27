package net.adultart.imageservice.repository;

import net.adultart.imageservice.model.ImageTag;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ImageTagRepositoryImpl implements ImageTagRepository {
    @Override
    public Optional<ImageTag> findByTag(String tag) {
        return find("tag", tag).firstResultOptional();
    }
}
