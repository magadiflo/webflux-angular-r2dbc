package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.exception.ItemNotFoundException;
import dev.magadiflo.app.exception.UnexpectedItemVersionException;
import dev.magadiflo.app.exception.VersionNotProvidedException;
import dev.magadiflo.app.mapper.ItemMapper;
import dev.magadiflo.app.mapper.TagMapper;
import dev.magadiflo.app.model.dto.ItemResource;
import dev.magadiflo.app.model.dto.ItemUpdateResource;
import dev.magadiflo.app.model.dto.NewItemResource;
import dev.magadiflo.app.model.entity.Item;
import dev.magadiflo.app.model.entity.ItemTag;
import dev.magadiflo.app.repository.ItemRepository;
import dev.magadiflo.app.repository.ItemTagRepository;
import dev.magadiflo.app.repository.PersonRepository;
import dev.magadiflo.app.repository.TagRepository;
import dev.magadiflo.app.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("last_modified_date"));

    private final PersonRepository personRepository;
    private final ItemRepository itemRepository;
    private final TagRepository tagRepository;
    private final ItemTagRepository itemTagRepository;
    private final ItemMapper itemMapper;
    private final TagMapper tagMapper;


    @Override
    public Flux<ItemResource> findAllItems() {
        return this.itemRepository.findAll(DEFAULT_SORT)
                .flatMap(this::loadRelations)
                .map(this.itemMapper::toItemResource);
    }

    @Override
    public Mono<ItemResource> findItemById(Long itemId, boolean loadRelations) {
        Mono<Item> itemDB = this.findAndItemById(itemId, null);
        return loadRelations ?
                itemDB.flatMap(this::loadRelations).map(this.itemMapper::toItemResource) :
                itemDB.map(this.itemMapper::toItemResource);
    }

    @Override
    @Transactional
    public Mono<ItemResource> createItem(NewItemResource newItemResource) {
        return this.itemRepository.save(this.itemMapper.toItem(newItemResource))
                .flatMap(itemDB -> {
                    Collection<ItemTag> itemTags = this.tagMapper.toItemTags(itemDB.getId(), newItemResource.getTagIds());
                    return this.itemTagRepository
                            .saveAll(itemTags)
                            .collectList()
                            .thenReturn(this.itemMapper.toItemResource(itemDB));
                });
    }

    @Override
    @Transactional
    public Mono<ItemResource> updateItem(Long itemId, ItemUpdateResource itemUpdateResource, Long version) {
        if (version == null) {
            return Mono.error(new VersionNotProvidedException());
        }
        return this.findAndItemById(itemId, version)
                .flatMap(itemDB -> this.itemTagRepository.findAllByItemId(itemDB.getId()).collectList()
                        .flatMap(currentItemTags -> {
                            Collection<Long> existingTagIds = this.tagMapper.extractTagIdsFromItemTags(currentItemTags);
                            Collection<Long> tagIdsToSave = itemUpdateResource.getTagIds();

                            // Item Tags a ser eliminados
                            Collection<ItemTag> removedItemTags = currentItemTags.stream()
                                    .filter(itemTag -> !tagIdsToSave.contains(itemTag.getTagId()))
                                    .toList();

                            // Item Tags a ser insertados
                            Collection<ItemTag> addedItemTags = tagIdsToSave.stream()
                                    .filter(tagId -> !existingTagIds.contains(tagId))
                                    .map(tagId -> ItemTag.builder().itemId(itemId).tagId(tagId).build())
                                    .toList();

                            return this.itemTagRepository.deleteAll(removedItemTags)
                                    .then(this.itemTagRepository.saveAll(addedItemTags).collectList())
                                    .thenReturn(itemDB);
                        })
                )
                .flatMap(itemDB -> this.itemRepository.save(this.itemMapper.update(itemUpdateResource, itemDB)))
                .flatMap(this::loadRelations)
                .map(this.itemMapper::toItemResource);
    }

    @Override
    @Transactional
    public Mono<Void> deleteItemById(Long itemId, Long version) {
        return this.findAndItemById(itemId, version)
                .zipWith(this.itemTagRepository.deleteAllByItemId(itemId), (itemDB, affectedRows) -> itemDB)
                .flatMap(this.itemRepository::delete);
    }

    private Mono<Item> loadRelations(Item item) {
        Mono<Item> itemMono = Mono.just(item)
                .zipWith(this.tagRepository.findTagsByItemId(item.getId()).collectList(), (itemToReturn, tags) -> {
                    itemToReturn.setTags(tags);
                    return itemToReturn;
                });
        if (item.getAssigneeId() != null) {
            itemMono = itemMono.zipWith(this.personRepository.findById(item.getAssigneeId()), (itemReturn, person) -> {
                itemReturn.setAssignee(person);
                return itemReturn;
            });
        }

        return itemMono;
    }

    private Mono<Item> findAndItemById(Long itemId, Long expectedVersion) {
        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .handle((itemDB, itemSynchronousSink) -> {
                    // Bloqueo optimista: comprobación previa
                    if (expectedVersion != null && !expectedVersion.equals(itemDB.getVersion())) {
                        itemSynchronousSink.error(new UnexpectedItemVersionException(expectedVersion, itemDB.getVersion()));
                    } else {
                        itemSynchronousSink.next(itemDB);
                    }
                });
    }
}
