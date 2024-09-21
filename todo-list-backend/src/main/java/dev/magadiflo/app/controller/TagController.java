package dev.magadiflo.app.controller;

import dev.magadiflo.app.model.dto.TagResource;
import dev.magadiflo.app.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<TagResource>>> findAllTags() {
        return Mono.just(ResponseEntity.ok(this.tagService.findAllTags()));
    }

    @GetMapping(path = "/{tagId}")
    public Mono<ResponseEntity<TagResource>> findTag(@PathVariable Long tagId) {
        return this.tagService.findTagById(tagId)
                .map(ResponseEntity::ok);
    }
}
