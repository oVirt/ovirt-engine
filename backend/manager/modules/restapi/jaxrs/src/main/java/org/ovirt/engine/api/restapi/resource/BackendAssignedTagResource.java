package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.businessentities.Tags;

public class BackendAssignedTagResource
    extends AbstractBackendSubResource<Tag, Tags>
    implements AssignedTagResource {

    private AbstractBackendAssignedTagsResource parent;

    public BackendAssignedTagResource(String id, AbstractBackendAssignedTagsResource parent) {
        super(id, Tag.class, Tags.class);
        this.parent = parent;
    }

    public AbstractBackendAssignedTagsResource getParent() {
        return parent;
    }

    @Override
    public Tag get() {
        return addLinks(map(parent.lookupTagById(guid)));
    }

    @Override
    protected Tag addParents(Tag tag) {
        return parent.addParents(tag);
    }

    @Override
    protected Tag doPopulate(Tag model, Tags entity) {
        return model;
    }
}
