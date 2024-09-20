package dev.magadiflo.app.exception;

public class UnexpectedItemVersionException extends RuntimeException {
    public UnexpectedItemVersionException(Long expectedVersion, Long foundVersion) {
        super("El item tiene una versión diferente a la esperada. Se esperaba [%d], se encontró [%d]".formatted(expectedVersion, foundVersion));
    }
}
