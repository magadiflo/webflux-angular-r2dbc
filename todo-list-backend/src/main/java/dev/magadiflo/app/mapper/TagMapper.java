package dev.magadiflo.app.mapper;

import dev.magadiflo.app.model.dto.TagResource;
import dev.magadiflo.app.model.entity.ItemTag;
import dev.magadiflo.app.model.entity.Tag;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagResource toTagResource(Tag tag);

    default List<Tag> toTags(Collection<Long> tagsId) {
        if (tagsId == null) return new ArrayList<>();

        return tagsId.stream()
                .map(tagId -> Tag.builder().id(tagId).build())
                .collect(Collectors.toList());
    }

    default Collection<Long> extractTagIdsFromTags(Collection<Tag> tags) {
        if (tags == null) return new LinkedHashSet<>();

        return tags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
    }

    default Collection<Long> extractTagIdsFromItemTags(Collection<ItemTag> itemTags) {
        if (itemTags == null) return new LinkedHashSet<>();

        return itemTags.stream()
                .map(ItemTag::getTagId)
                .collect(Collectors.toSet());
    }

    default Collection<ItemTag> toItemTags(Long itemId, Collection<Tag> tags) {
        if (tags == null) return new LinkedHashSet<>();

        return tags.stream()
                .map(tag -> ItemTag.builder()
                        .itemId(itemId)
                        .tagId(tag.getId())
                        .build())
                .collect(Collectors.toSet());
    }

}
