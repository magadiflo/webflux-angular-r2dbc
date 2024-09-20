package dev.magadiflo.app.exception;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(Long tagId) {
        super("No se encuentra el tag [%d]".formatted(tagId));
    }
}
