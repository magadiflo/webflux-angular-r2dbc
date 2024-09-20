package dev.magadiflo.app.exception;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(Long itemId) {
        super("No se encuentra el item [%d]".formatted(itemId));
    }
}
