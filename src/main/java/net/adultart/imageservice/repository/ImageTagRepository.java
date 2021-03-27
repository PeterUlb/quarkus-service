package net.adultart.imageservice.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import net.adultart.imageservice.model.ImageTag;

import java.util.Optional;

public interface ImageTagRepository extends PanacheRepository<ImageTag> {
    Optional<ImageTag> findByTag(String tag);
}
