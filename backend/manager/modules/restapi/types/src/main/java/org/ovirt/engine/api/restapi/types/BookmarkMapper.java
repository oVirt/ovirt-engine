package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.api.restapi.utils.GuidUtils;

public class BookmarkMapper {
    /**
     * Map the generated REST bookmark model to the business entity bookmark model.
     * @param model The REST bookmark model
     * @param template The business entity bookmark model template.
     * @return A business entity bookmark model filled from the REST model.
     */
    @Mapping(from = Bookmark.class, to = org.ovirt.engine.core.common.businessentities.Bookmark.class)
    public static org.ovirt.engine.core.common.businessentities.Bookmark map(Bookmark model,
            org.ovirt.engine.core.common.businessentities.Bookmark template) {
        org.ovirt.engine.core.common.businessentities.Bookmark entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.Bookmark();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetValue()) {
            entity.setValue(model.getValue());
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        return entity;
    }

    /**
     * Map a business entity bookmark model to a generated REST bookmark model.
     * @param entity The business entity bookmark model.
     * @param template The REST bookmark model template to use.
     * @return A REST bookmark model filled from the business entity.
     */
    @Mapping(from = org.ovirt.engine.core.common.businessentities.Bookmark.class, to = Bookmark.class)
    public static Bookmark map(org.ovirt.engine.core.common.businessentities.Bookmark entity, Bookmark template) {
        Bookmark model = template != null ? template : new Bookmark();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setValue(entity.getValue());
        return model;
    }
}
