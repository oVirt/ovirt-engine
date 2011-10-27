package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.ad_groups;

public class AbstractBackendGroupResource
    extends AbstractBackendSubResource<Group, ad_groups> {

    protected AbstractBackendGroupsResource parent;

    public AbstractBackendGroupResource(String id, AbstractBackendGroupsResource parent) {
        super(id, Group.class, ad_groups.class);
        this.parent = parent;
    }

    public Group get() {
        Group entity = parent.lookupGroup(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(entity);
    }

    AbstractBackendGroupsResource getParent() {
        return parent;
    }
}
