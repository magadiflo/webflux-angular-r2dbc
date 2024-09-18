package dev.magadiflo.app.mapper;

import dev.magadiflo.app.model.dto.ItemPatchResource;
import dev.magadiflo.app.model.dto.ItemResource;
import dev.magadiflo.app.model.dto.ItemUpdateResource;
import dev.magadiflo.app.model.dto.NewItemResource;
import dev.magadiflo.app.model.entity.Item;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PersonMapper.class, TagMapper.class})
public abstract class ItemMapper {

    @Autowired
    private TagMapper tagMapper;

    public abstract ItemResource toItemResource(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    public abstract Item toItem(NewItemResource itemResource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    public abstract Item update(ItemUpdateResource itemUpdateResource, @MappingTarget Item item);

    @AfterMapping
    public void afterMapping(NewItemResource itemResource, @MappingTarget Item item) {
        item.setTags(this.tagMapper.toTags(itemResource.getTagIds()));
    }

    @AfterMapping
    public void afterMapping(ItemUpdateResource itemResource, @MappingTarget Item item) {
        item.setTags(tagMapper.toTags(itemResource.getTagIds()));
    }

    public Item patch(ItemPatchResource patchResource, Item item) {
        if (patchResource.getDescription() != null) {
            item.setDescription(patchResource.getDescription());
        }

        if (patchResource.getStatus() != null) {
            item.setStatus(patchResource.getStatus());
        }

        if (patchResource.getAssigneeId() != null) {
            item.setAssigneeId(patchResource.getAssigneeId());
        }

        if (patchResource.getTagIds() != null) {
            item.setTags(this.tagMapper.toTags(patchResource.getTagIds()));
        }

        return item;
    }

}
