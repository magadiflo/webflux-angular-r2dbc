package dev.magadiflo.app.handle;

import dev.magadiflo.app.exception.*;
import dev.magadiflo.app.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            ItemNotFoundException.class,
            PersonNotFoundException.class,
            TagNotFoundException.class
    })
    public Mono<ResponseEntity<ResponseMessage<Void>>> handle(NotFoundException exception) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseMessage.<Void>builder().message(exception.getMessage()).build()));
    }

    @ExceptionHandler(UnexpectedItemVersionException.class)
    public Mono<ResponseEntity<ResponseMessage<Void>>> handle(UnexpectedItemVersionException exception) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(ResponseMessage.<Void>builder().message(exception.getMessage()).build()));
    }

    @ExceptionHandler(VersionNotProvidedException.class)
    public Mono<ResponseEntity<ResponseMessage<Void>>> handle(VersionNotProvidedException exception) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.<Void>builder().message(exception.getMessage()).build()));
    }
}
