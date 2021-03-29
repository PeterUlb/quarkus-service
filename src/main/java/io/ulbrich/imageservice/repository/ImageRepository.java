package io.ulbrich.imageservice.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.ulbrich.imageservice.model.Image;

import java.util.Optional;

public interface ImageRepository extends PanacheRepository<Image> {
    Optional<Image> findByExternalId(String externalKey);
}
