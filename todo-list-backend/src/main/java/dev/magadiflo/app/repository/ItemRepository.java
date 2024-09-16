package dev.magadiflo.app.repository;

import dev.magadiflo.app.model.entity.Item;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
}
