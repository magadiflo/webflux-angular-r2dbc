package dev.magadiflo.app.exception;

public class PersonNotFoundException extends NotFoundException {
    public PersonNotFoundException(Long personId) {
        super("No se encuentra el person [%d]".formatted(personId));
    }
}
