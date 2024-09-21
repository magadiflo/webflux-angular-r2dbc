package dev.magadiflo.app.controller;

import dev.magadiflo.app.model.dto.PersonResource;
import dev.magadiflo.app.service.PersonService;
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
@RequestMapping(path = "/api/v1/persons")
public class PersonController {

    private final PersonService personService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<PersonResource>>> findAllPersons() {
        return Mono.just(ResponseEntity.ok(this.personService.findAllPersons()));
    }

    @GetMapping(path = "/{personId}")
    public Mono<ResponseEntity<PersonResource>> findPerson(@PathVariable Long personId) {
        return this.personService.findPersonById(personId)
                .map(ResponseEntity::ok);
    }
}
