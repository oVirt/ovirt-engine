package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.api.utils.ReflectionHelper.assignChildModel;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.resource.aaa.DomainGroupsResource;
import org.ovirt.engine.api.resource.aaa.DomainResource;
import org.ovirt.engine.api.resource.aaa.DomainUsersResource;
import org.ovirt.engine.api.restapi.model.Directory;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendDomainResource extends AbstractBackendSubResource<Domain, Directory>
implements DomainResource {
    private String id;
    private BackendDomainsResource parent;

    public BackendDomainResource(String id, BackendDomainsResource parent) {
        super(id, Domain.class, Directory.class);
        this.id = id;
        this.parent = parent;
    }

    @Override
    public Domain get() {
        Domain domain = parent.lookupDirectoryById(id, true);
        return injectSearchLinks(addLinks(domain), subCollections);
    }

    public Domain getDirectory() {
        return parent.lookupDirectoryById(id, false);
    }

    @Override
    public DomainGroupsResource getGroupsResource() {
        return inject(new BackendDomainGroupsResource(id, this));
    }

    @Override
    public DomainUsersResource getUsersResource() {
        return inject(new BackendDomainUsersResource(id, this));
    }

    @Override
    protected Domain addParents(Domain domain) {
        if(parent!=null){
            assignChildModel(domain, Domain.class).setId(id);
        }
        return domain;
    }

    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }
}
