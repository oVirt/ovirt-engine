package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.resource.DomainGroupResource;

public class BackendDomainGroupResource
    extends AbstractBackendGroupResource
    implements DomainGroupResource {

    public BackendDomainGroupResource(String id, BackendDomainGroupsResource parent) {
        super(id, parent);
    }

    @Override
    public Group addParents(Group group) {
        return parent.addParents(group);
    }
}
