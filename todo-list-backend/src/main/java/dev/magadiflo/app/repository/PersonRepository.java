package dev.magadiflo.app.repository;

import dev.magadiflo.app.model.entity.Person;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PersonRepository extends R2dbcRepository<Person, Long> {
}
