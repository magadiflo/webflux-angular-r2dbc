package dev.magadiflo.app.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "items_tags")
public class ItemTag {
    @Id
    private Long id;
    private Long itemId;
    private Long tagId;
}
