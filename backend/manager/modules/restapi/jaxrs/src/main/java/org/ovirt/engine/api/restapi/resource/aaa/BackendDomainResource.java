package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.api.utils.ReflectionHelper.assignChildModel;
import static org.ovirt.engine.api.restapi.resource.aaa.BackendDomainsResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.resource.aaa.DomainGroupsResource;
import org.ovirt.engine.api.resource.aaa.DomainResource;
import org.ovirt.engine.api.resource.aaa.DomainUsersResource;
import org.ovirt.engine.api.restapi.model.Directory;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;

public class BackendDomainResource extends AbstractBackendSubResource<Domain, Directory>
implements DomainResource {
    private String id;
    private BackendDomainsResource parent;

    public BackendDomainResource(String id, BackendDomainsResource parent) {
        super(id, Domain.class, Directory.class, SUB_COLLECTIONS);
        this.id = id;
        this.parent = parent;
    }

    @Override
    public Domain get() {
        Domain domain = parent.lookupDirectoryById(id, true);
        return injectSearchLinks(addLinks(domain), SUB_COLLECTIONS);
    }

    public Domain getDirectory() {
        return parent.lookupDirectoryById(id, false);
    }

    @Override
    public DomainGroupsResource getDomainGroupsResource() {
        return inject(new BackendDomainGroupsResource(id, this));
    }

    @Override
    public DomainUsersResource getDomainUsersResource() {
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
    protected Domain doPopulate(Domain model, Directory entity) {
        return model;
    }
}
