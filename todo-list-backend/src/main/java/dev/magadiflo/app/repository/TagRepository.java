package dev.magadiflo.app.repository;

import dev.magadiflo.app.model.entity.Tag;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TagRepository extends R2dbcRepository<Tag, Long> {
    @Query("""
            SELECT t.id, t.name, t.version, t.created_date, t.last_modified_date
            FROM tags AS t
                INNER JOIN items_tags AS it ON(t.id = it.tag_id)
            WHERE it.item_id = :itemId
            ORDER BY t.name
            """)
    Flux<Tag> findTagsByItemId(Long itemId);
}
