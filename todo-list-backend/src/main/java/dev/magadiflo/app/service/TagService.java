package dev.magadiflo.app.service;

import dev.magadiflo.app.model.dto.TagResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TagService {
    Flux<TagResource> findAllTags();

    Mono<TagResource> findTagById(Long tagId);
}
