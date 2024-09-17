package dev.magadiflo.app.model.dto;

import dev.magadiflo.app.model.enums.ItemStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ItemResource {
    private Long id;
    private String description;
    private ItemStatus status;

    private PersonResource assignee;
    private List<TagResource> tags;

    private Long version;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
