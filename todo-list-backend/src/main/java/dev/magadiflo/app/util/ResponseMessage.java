package dev.magadiflo.app.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponseMessage<T> {
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T content;
}
