package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;

public class TagMapper {

    @Mapping(from = Tag.class, to = Tags.class)
    public static Tags map(Tag model, Tags template) {
        Tags entity = template != null ? template : new Tags();
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

    @Mapping(from = Tags.class, to = Tag.class)
    public static Tag map(Tags entity, Tag template) {
        Tag model = template != null ? template : new Tag();
        model.setId(entity.gettag_id().toString());
        model.setName(entity.gettag_name());
        model.setDescription(entity.getdescription());
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
        if (entity.getparent_id() != null) {
            Tag parent = new Tag();
            parent.setId(entity.getparent_id().toString());
            return parent;
        } else {
            return null;
        }
    }
}
