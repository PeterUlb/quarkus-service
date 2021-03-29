package io.ulbrich.imageservice.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.ulbrich.imageservice.model.ImageTag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ImageTagRepository extends PanacheRepository<ImageTag> {
    Optional<ImageTag> findByTag(String tag);

    List<ImageTag> findByTags(Set<String> tags);
}
