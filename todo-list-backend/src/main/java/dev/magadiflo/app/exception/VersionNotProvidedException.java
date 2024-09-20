package dev.magadiflo.app.exception;

public class VersionNotProvidedException extends RuntimeException {
    public VersionNotProvidedException() {
        super("Al actualizar un item, se deben proporcionar la versión");
    }
}
