package dev.magadiflo.app.controller;

import dev.magadiflo.app.model.dto.ItemResource;
import dev.magadiflo.app.model.dto.ItemUpdateResource;
import dev.magadiflo.app.model.dto.NewItemResource;
import dev.magadiflo.app.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<ItemResource>>> findAllItems() {
        return Mono.just(ResponseEntity.ok(this.itemService.findAllItems()));
    }

    @GetMapping(path = "/{itemId}")
    public Mono<ResponseEntity<ItemResource>> findItem(@PathVariable Long itemId,
                                                       @RequestParam(required = false, defaultValue = "false") Boolean loadRelations) {
        return this.itemService.findItemById(itemId, loadRelations)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<ItemResource>> createItem(@Valid @RequestBody NewItemResource newItemResource) {
        return this.itemService.createItem(newItemResource)
                .map(itemResource -> new ResponseEntity<>(itemResource, HttpStatus.CREATED));
    }

    @PutMapping(path = "/{itemId}")
    public Mono<ResponseEntity<ItemResource>> updateItem(@PathVariable Long itemId,
                                                         @Valid @RequestBody ItemUpdateResource itemUpdateResource,
                                                         @RequestHeader(value = HttpHeaders.IF_MATCH) Long version) {
        return this.itemService.updateItem(itemId, itemUpdateResource, version)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{itemId}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable Long itemId,
                                                 @RequestHeader(value = HttpHeaders.IF_MATCH) Long version) {
        return this.itemService.deleteItemById(itemId, version)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
