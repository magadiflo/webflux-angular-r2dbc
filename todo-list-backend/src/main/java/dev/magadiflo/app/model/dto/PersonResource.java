package dev.magadiflo.app.model.dto;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PersonResource {
    private Long id;
    private String firstName;
    private String lastName;
}
