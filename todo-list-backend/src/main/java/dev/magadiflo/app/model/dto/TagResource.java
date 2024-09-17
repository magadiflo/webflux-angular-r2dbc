package dev.magadiflo.app.model.dto;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TagResource {
    private Long id;
    private String name;
}
