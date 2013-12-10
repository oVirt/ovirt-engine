package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.resource.DomainGroupResource;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * This resource corresponds to a group that exists in some directory accessible by the engine, and that may or may not
 * have been added to the engine and stored in the database. This resource doesn't provide information about the
 * permissions, roles or tags of the group, even if those have been already assigned and stored in the database.
 */
public class BackendDomainGroupResource
        extends AbstractBackendSubResource<Group, LdapGroup>
        implements DomainGroupResource {

    private BackendDomainGroupsResource parent;

    public BackendDomainGroupResource(String id, BackendDomainGroupsResource parent) {
        super(id, Group.class, LdapGroup.class);
        this.parent = parent;
    }

    public BackendDomainGroupsResource getParent() {
        return parent;
    }

    public void setParent(BackendDomainGroupsResource parent) {
        this.parent = parent;
    }

    @Override
    public Group get() {
        DirectoryIdQueryParameters queryParameters = new DirectoryIdQueryParameters(
            parent.getDirectory().getName(),
            guid
        );
        return performGet(VdcQueryType.GetDirectoryGroupById, queryParameters, BaseResource.class);
    }

    @Override
    protected Group doPopulate(Group model, LdapGroup entity) {
        return model;
    }

}
