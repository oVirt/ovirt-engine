package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;

public class TagMapper {

    @Mapping(from = Tag.class, to = Tags.class)
    public static Tags map(Tag model, Tags template) {
        Tags entity = template != null ? template : new Tags();
        entity.setParentId(parent(model, entity.getParentId()));
        if (model.isSetId()) {
            entity.setTagId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setTagName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        return entity;
    }

    @Mapping(from = Tags.class, to = Tag.class)
    public static Tag map(Tags entity, Tag template) {
        Tag model = template != null ? template : new Tag();
        model.setId(entity.getTagId().toString());
        model.setName(entity.getTagName());
        model.setDescription(entity.getDescription());
        model.setParent(parent(entity));
        return model;
    }

    private static Guid parent(Tag model, Guid current) {
        if (model.isSetParent() && model.getParent().isSetId()) {
            return GuidUtils.asGuid(model.getParent().getId());
        } else {
            return current == null ? Guid.Empty : current;
        }
    }

    private static Tag parent(Tags entity) {
        if (entity.getParentId() != null) {
            Tag parent = new Tag();
            parent.setId(entity.getParentId().toString());
            return parent;
        } else {
            return null;
        }
    }
}
