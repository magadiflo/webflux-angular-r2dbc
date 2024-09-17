package dev.magadiflo.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class NewItemResource {
    @NotBlank
    @Size(max = 4000)
    private String description;
    private Long assigneeId;
    private Set<Long> tagIds;
}
