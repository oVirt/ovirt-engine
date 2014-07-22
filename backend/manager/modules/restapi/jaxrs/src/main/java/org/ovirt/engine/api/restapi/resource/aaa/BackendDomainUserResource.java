package org.ovirt.engine.api.restapi.resource.aaa;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.aaa.DomainUserResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to an user that exists in some directory
 * accessible by the engine, and that may or may not have been added to
 * the engine and stored in the database. This resource doesn't provide
 * information about the permissions, roles or tags of the user, even if
 * those have been already assigned and stored in the database.
 */
public class BackendDomainUserResource
        extends AbstractBackendSubResource<User, DirectoryUser>
        implements DomainUserResource {

    private BackendDomainUsersResource parent;

    public BackendDomainUserResource(String id, BackendDomainUsersResource parent) {
        super(DirectoryEntryIdUtils.decode(id), User.class, DirectoryUser.class);
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
        String directory = parent.getDirectory().getName();
        DirectoryIdQueryParameters parameters = new DirectoryIdQueryParameters(directory, id);
        return performGet(VdcQueryType.GetDirectoryUserById, parameters, BaseResource.class);
    }

    @Override
    protected User doPopulate(User model, DirectoryUser entity) {
        return model;
    }

    // We need to override this method because the native identifier of this
    // resource isn't an UUID.
    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }
}
