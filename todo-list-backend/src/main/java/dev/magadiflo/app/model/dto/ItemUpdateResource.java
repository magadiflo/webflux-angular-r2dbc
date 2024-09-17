package dev.magadiflo.app.model.dto;

import dev.magadiflo.app.model.enums.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ItemUpdateResource {
    @NotBlank
    @Size(max = 4000)
    private String description;
    @NotNull
    private ItemStatus status;
    private Long assigneeId;
    private Set<Long> tagIds;
}
