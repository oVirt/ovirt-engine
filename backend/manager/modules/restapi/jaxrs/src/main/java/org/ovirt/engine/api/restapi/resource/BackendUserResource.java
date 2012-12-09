package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.UserResource;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.ovirt.engine.api.restapi.resource.BackendUsersResourceBase.SUB_COLLECTIONS;

public class BackendUserResource
        extends AbstractBackendSubResource<User, DbUser>
        implements UserResource {

    private BackendUsersResource parent;

    public BackendUserResource(String id, BackendUsersResource parent) {
        super(id, User.class, DbUser.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public User get() {
        return performGet(VdcQueryType.GetDbUserByUserId, new GetDbUserByUserIdParameters(guid), BaseResource.class);
    }

    @Override
    public AssignedRolesResource getRolesResource() {
        return inject(new BackendAssignedRolesResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendUserTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsByAdElementId,
                                                             new MultilevelAdministrationByAdElementIdParameters(guid),
                                                             User.class));
    }

    public BackendUsersResource getParent() {
        return parent;
    }

    @Override
    protected User doPopulate(User model, DbUser entity) {
        return model;
    }
}
