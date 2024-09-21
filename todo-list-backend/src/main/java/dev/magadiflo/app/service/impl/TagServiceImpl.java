package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.exception.TagNotFoundException;
import dev.magadiflo.app.mapper.TagMapper;
import dev.magadiflo.app.model.dto.TagResource;
import dev.magadiflo.app.repository.TagRepository;
import dev.magadiflo.app.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("name"));
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public Flux<TagResource> findAllTags() {
        return this.tagRepository.findAll(DEFAULT_SORT)
                .map(this.tagMapper::toTagResource);
    }

    @Override
    public Mono<TagResource> findTagById(Long tagId) {
        return this.tagRepository.findById(tagId)
                .switchIfEmpty(Mono.error(new TagNotFoundException(tagId)))
                .map(this.tagMapper::toTagResource);
    }
}
