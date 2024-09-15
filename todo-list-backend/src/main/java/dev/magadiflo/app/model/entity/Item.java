package dev.magadiflo.app.model.entity;

import dev.magadiflo.app.model.enums.ItemStatus;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "items")
public class Item {
    @Id
    private Long id;
    private String description;
    @Builder.Default
    private ItemStatus status = ItemStatus.TO_DO;
    private Long assigneeId;

    @Transient
    private Person assignee;
    @Transient
    private List<Tag> tags;

    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
