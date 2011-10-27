package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.DomainUserResource;

public class BackendDomainUserResource
    extends AbstractBackendUserResource
    implements DomainUserResource {

    public BackendDomainUserResource(String id, BackendDomainUsersResource parent) {
        super(id, parent);
    }

    @Override
    public User addParents(User user) {
        return parent.addParents(user);
    }
}
