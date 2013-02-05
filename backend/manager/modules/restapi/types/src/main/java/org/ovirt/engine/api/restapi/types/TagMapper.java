package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.TagParent;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.NGuid;

public class TagMapper {

    @Mapping(from = Tag.class, to = tags.class)
    public static tags map(Tag model, tags template) {
        tags entity = template != null ? template : new tags();
        entity.setparent_id(parent(model, entity.getparent_id()));
        if (model.isSetId()) {
            entity.settag_id(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.settag_name(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        return entity;
    }

    @Mapping(from = tags.class, to = Tag.class)
    public static Tag map(tags entity, Tag template) {
        Tag model = template != null ? template : new Tag();
        model.setId(entity.gettag_id().toString());
        model.setName(entity.gettag_name());
        model.setDescription(entity.getdescription());
        model.setParent(parent(entity));
        return model;
    }

    private static NGuid parent(Tag model, NGuid current) {
        if (model.isSetParent() &&
            model.getParent().isSetTag() &&
            model.getParent().getTag().isSetId()) {
            return GuidUtils.asGuid(model.getParent().getTag().getId());
        } else {
            return current == null ? NGuid.Empty : current;
        }
    }

    private static TagParent parent(tags entity) {
        if (entity.getparent_id() != null) {
            TagParent parent = new TagParent();
            parent.setTag(new Tag());
            parent.getTag().setId(entity.getparent_id().toString());
            return parent;
        } else {
            return null;
        }
    }
}
