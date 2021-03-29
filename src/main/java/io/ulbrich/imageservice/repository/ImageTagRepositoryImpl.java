package io.ulbrich.imageservice.repository;

import io.quarkus.panache.common.Parameters;
import io.ulbrich.imageservice.model.ImageTag;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ImageTagRepositoryImpl implements ImageTagRepository {
    @Override
    public Optional<ImageTag> findByTag(String tag) {
        return find("tag", tag).firstResultOptional();
    }

    @Override
    public List<ImageTag> findByTags(Set<String> tags) {
        return list("FROM ImageTag imageTag WHERE imageTag.tag IN :tags", Parameters.with("tags", tags));
    }
}
