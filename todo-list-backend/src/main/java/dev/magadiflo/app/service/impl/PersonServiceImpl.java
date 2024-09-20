package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.mapper.PersonMapper;
import dev.magadiflo.app.model.dto.PersonResource;
import dev.magadiflo.app.repository.PersonRepository;
import dev.magadiflo.app.service.PersonService;
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
public class PersonServiceImpl implements PersonService {

    // Los nombres de los campos por lo que se ordenarán son los nombres de los campos de la base de datos.
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("first_name"), Sort.Order.by("last_name"));

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public Flux<PersonResource> findAllPersons() {
        return this.personRepository.findAll(DEFAULT_SORT)
                .map(this.personMapper::toPersonResource);
    }

    @Override
    public Mono<PersonResource> findPersonById(Long personId) {
        return this.personRepository.findById(personId)
                .map(this.personMapper::toPersonResource);
    }
}
