package dev.magadiflo.app.service;

import dev.magadiflo.app.model.dto.PersonResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonService {
    Flux<PersonResource> findAllPersons();

    Mono<PersonResource> findPersonById(Long personId);
}
