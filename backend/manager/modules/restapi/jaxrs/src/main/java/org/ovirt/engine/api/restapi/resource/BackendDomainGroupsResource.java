package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.resource.DomainGroupResource;
import org.ovirt.engine.api.resource.DomainGroupsResource;

public class BackendDomainGroupsResource extends AbstractBackendGroupsResource
implements DomainGroupsResource {

    private String directoryId;

    public BackendDomainGroupsResource(String id, BackendDomainResource parent) {
        super(id,parent);
        this.directoryId = id;
    }

    @Override
    public Group addParents(Group user) {
        user.setDomain(new Domain());
        user.getDomain().setId(directoryId);
        return user;
    }

    @Override
    @SingleEntityResource
    public DomainGroupResource getDomainGroupSubResource(String id) {
        return inject(new BackendDomainGroupResource(id, this));
    }

    @Override
    public Groups list() {
        return mapDomainGroupsCollection(getGroupsFromDomain());
    }

}
