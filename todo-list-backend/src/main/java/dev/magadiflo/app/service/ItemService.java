package dev.magadiflo.app.service;

import dev.magadiflo.app.model.dto.ItemResource;
import dev.magadiflo.app.model.dto.ItemUpdateResource;
import dev.magadiflo.app.model.dto.NewItemResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemService {
    Flux<ItemResource> findAllItems();

    Mono<ItemResource> findItemById(Long itemId, boolean loadRelations);

    Mono<ItemResource> createItem(NewItemResource newItemResource);

    Mono<ItemResource> updateItem(Long itemId, ItemUpdateResource itemUpdateResource, Long version);

    Mono<Void> deleteItemById(Long itemId, Long version);
}
