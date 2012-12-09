package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.LdapGroup;

public class AbstractBackendGroupResource
    extends AbstractBackendSubResource<Group, LdapGroup> {

    protected AbstractBackendGroupsResource parent;

    public AbstractBackendGroupResource(String id, AbstractBackendGroupsResource parent) {
        super(id, Group.class, LdapGroup.class);
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

    @Override
    protected Group doPopulate(Group model, LdapGroup entity) {
        return model;
    }

}
