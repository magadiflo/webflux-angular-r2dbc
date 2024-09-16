package dev.magadiflo.app.repository;

import dev.magadiflo.app.model.entity.ItemTag;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemTagRepository extends R2dbcRepository<ItemTag, Long> {
    Flux<ItemTag> findAllByItemId();

    Mono<Integer> deleteAllByItemId(Long itemId);
}
