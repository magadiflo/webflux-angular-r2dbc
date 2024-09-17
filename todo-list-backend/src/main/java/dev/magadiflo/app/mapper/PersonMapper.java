package dev.magadiflo.app.mapper;

import dev.magadiflo.app.model.dto.PersonResource;
import dev.magadiflo.app.model.entity.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    PersonResource toPersonResource(Person person);
}
