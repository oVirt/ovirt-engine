package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.DomainUserResource;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * This resource corresponds to an user that exists in some directory
 * accessible by the engine, and that may or may not have been added to
 * the engine and stored in the database. This resource doesn't provide
 * information about the permissions, roles or tags of the user, even if
 * those have been already assigned and stored in the database.
 */
public class BackendDomainUserResource
        extends AbstractBackendSubResource<User, LdapUser>
        implements DomainUserResource {

    private BackendDomainUsersResource parent;

    public BackendDomainUserResource(String id, BackendDomainUsersResource parent) {
        super(id, User.class, LdapUser.class);
        this.parent = parent;
    }

    public BackendDomainUsersResource getParent() {
        return parent;
    }

    public void setParent(BackendDomainUsersResource parent) {
        this.parent = parent;
    }

    @Override
    public User get() {
        DirectoryIdQueryParameters queryParameters = new DirectoryIdQueryParameters(
            parent.getDirectory().getName(),
            guid
        );
        return performGet(VdcQueryType.GetDirectoryUserById, queryParameters, BaseResource.class);
    }

    @Override
    protected User doPopulate(User model, LdapUser entity) {
        return model;
    }

}
