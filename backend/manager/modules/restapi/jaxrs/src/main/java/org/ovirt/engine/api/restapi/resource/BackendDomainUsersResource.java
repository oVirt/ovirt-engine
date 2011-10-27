package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.DomainUserResource;
import org.ovirt.engine.api.resource.DomainUsersResource;

public class BackendDomainUsersResource extends AbstractBackendUsersResource implements DomainUsersResource {

    private String directoryId;

    public BackendDomainUsersResource(String id, BackendDomainResource parent) {
        super(id,parent);
        this.directoryId = id;
    }

    @Override
    public User addParents(User user) {
        user.setDomain(new Domain());
        user.getDomain().setId(directoryId);
        return user;
    }

    @Override
    @SingleEntityResource
    public DomainUserResource getDomainUserSubResource(String id) {
        return inject(new BackendDomainUserResource(id, this));
    }
}
