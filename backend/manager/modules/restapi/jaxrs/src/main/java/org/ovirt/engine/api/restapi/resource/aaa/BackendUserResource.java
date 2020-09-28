package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.EventSubscriptionsResource;
import org.ovirt.engine.api.resource.aaa.DomainUserGroupsResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.api.resource.aaa.UserResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedPermissionsResource;
import org.ovirt.engine.api.restapi.resource.BackendAssignedRolesResource;
import org.ovirt.engine.api.restapi.resource.BackendEventSubscriptionsResource;
import org.ovirt.engine.api.restapi.resource.BackendUserTagsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.UpdateUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to an user that has been added to the engine and
 * stored in the database.
 */
public class BackendUserResource
        extends AbstractBackendSubResource<User, DbUser>
        implements UserResource {

    private static final String MERGE = "merge";

    private static final String[] IMMUTABLE_FIELDS = {
            "department",
            "domainEntryId",
            "email",
            "lastName",
            "loggedIn",
            "namespace",
            "password",
            "principal",
            "userName",
            "domain",
            "groups",
            "permissions",
            "roles",
            "sshPublicKeys",
            "tags"};
    private BackendUsersResource parent;

    public BackendUserResource(String id, BackendUsersResource parent) {
        super(id, User.class, DbUser.class);
        this.parent = parent;
    }

    public void setParent(BackendUsersResource parent) {
        this.parent = parent;
    }

    public BackendUsersResource getParent() {
        return parent;
    }

    @Override
    public User get() {
        return performGet(QueryType.GetDbUserByUserId, new IdQueryParameters(guid), BaseResource.class);
    }

    @Override
    public User update(User user) {
        boolean mergeOptions = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, MERGE, false, false);
        return performUpdate(user,
                new QueryIdResolver<Guid>(QueryType.GetDbUserByUserId, IdQueryParameters.class),
                ActionType.UpdateUserOptions,
                new UpdateParametersProvider(mergeOptions));
    }

    public class UpdateParametersProvider implements ParametersProvider<User, DbUser> {
        private boolean mergeOptions;

        public UpdateParametersProvider(boolean mergeOptions) {
            this.mergeOptions = mergeOptions;
        }

        @Override
        public ActionParametersBase getParameters(User model, DbUser entity) {
            return new UpdateUserParameters(map(model, entity), mergeOptions);
        }
    }

    @Override
    protected String[] getStrictlyImmutable() {
        List<String> all = new ArrayList<>();
        all.addAll(Arrays.asList(super.getStrictlyImmutable()));
        all.addAll(Arrays.asList(IMMUTABLE_FIELDS));
        return all.toArray(new String[] {});
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
                                                             QueryType.GetPermissionsOnBehalfByAdElementId,
                                                             new IdQueryParameters(guid),
                                                             User.class));
    }

    @Override
    public SshPublicKeysResource getSshPublicKeysResource() {
        return inject(new BackendSSHPublicKeysResource(guid));
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveUser, new IdParameters(guid));
    }

    @Override
    public DomainUserGroupsResource getGroupsResource() {
        return inject(new BackendDomainUserGroupsResource(guid));
    }

    @Override
    public EventSubscriptionsResource getEventSubscriptionsResource() {
        return inject(new BackendEventSubscriptionsResource(id));
    }
}
