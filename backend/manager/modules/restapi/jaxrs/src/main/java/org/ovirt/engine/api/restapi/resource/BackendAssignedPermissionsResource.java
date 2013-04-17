package org.ovirt.engine.api.restapi.resource;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByPermissionIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class BackendAssignedPermissionsResource
        extends AbstractBackendCollectionResource<Permission, permissions>
        implements AssignedPermissionsResource, Serializable {

    private Guid targetId;
    private VdcQueryType queryType;
    private VdcQueryParametersBase queryParams;
    private Class<? extends BaseResource> suggestedParentType;
    private VdcObjectType objectType;

    public BackendAssignedPermissionsResource(Guid targetId,
                                              VdcQueryType queryType,
                                              VdcQueryParametersBase queryParams,
                                              Class<? extends BaseResource> suggestedParentType) {
        this(targetId, queryType, queryParams, suggestedParentType, null);
    }

    public BackendAssignedPermissionsResource(Guid targetId,
                                              VdcQueryType queryType,
                                              VdcQueryParametersBase queryParams,
                                              Class<? extends BaseResource> suggestedParentType,
                                              VdcObjectType objectType) {
        super(Permission.class, permissions.class);
        this.targetId = targetId;
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.suggestedParentType = suggestedParentType;
        this.objectType = objectType;
    }

    @Override
    public Permissions list() {
        Set<permissions> permissions = new TreeSet<permissions>(new PermissionsComparator());
        List<permissions> directPermissions = getBackendCollection(queryType, queryParams);
        permissions.addAll(directPermissions);
        if (queryType.equals(VdcQueryType.GetPermissionsForObject)) {
            permissions.addAll(getInheritedPermissions());
        }
        return mapCollection(permissions);
    }

    private List<permissions> getInheritedPermissions() {
        ((GetPermissionsForObjectParameters)queryParams).setVdcObjectType(objectType);
        ((GetPermissionsForObjectParameters)queryParams).setDirectOnly(false);
        List<permissions> inheritedPermissions = getBackendCollection(queryType, queryParams);
        for (permissions entity : inheritedPermissions) {
            if (objectType != null) {
                entity.setObjectType(objectType);
                entity.setObjectId(targetId);
            }
        }
        return inheritedPermissions;
    }

    static class PermissionsComparator implements Comparator<permissions>, Serializable {
        @Override
        public int compare(permissions o1, permissions o2) {
            String id1 = o1.getId().toString();
            String id2 = o2.getId().toString();
            return id1.compareTo(id2);
        }
    }

    @Override
    public Response add(Permission permission) {
        validateParameters(permission,
                           isPrincipalSubCollection()
                           ? new String[] {"role.id", "dataCenter|cluster|host|storageDomain|vm|vmpool|template.id"}
                           : new String[] {"role.id", "user|group.id"});
        permissions entity = map(permission, getPermissionsTemplate(permission));
        return performCreate(VdcActionType.AddPermission,
                               getPrincipal(entity, permission),
                               new QueryIdResolver<Guid>(VdcQueryType.GetPermissionById,
                                                   MultilevelAdministrationByPermissionIdParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemovePermission, new PermissionsOperationsParametes(getPermissions(id)));
    }

    @Override
    @SingleEntityResource
    public PermissionResource getPermissionSubResource(String id) {
        return inject(new BackendPermissionResource(id, this, suggestedParentType));
    }

    protected Permissions mapCollection(Set<permissions> entities) {
        Permissions collection = new Permissions();
        for (permissions entity : entities) {
             castEveryonePermissionsToUser(entity);
             Permission permission = map(entity, getUserById(entity.getad_element_id()));
             collection.getPermissions().add(addLinks(permission, permission.getUser() != null ? suggestedParentType : Group.class));
        }
        return collection;
    }

    private void castEveryonePermissionsToUser(permissions entity) {
        if (entity.getad_element_id() != null &&
            entity.getad_element_id().equals(Guid.EVERYONE) &&
            queryType.equals(VdcQueryType.GetPermissionsByAdElementId)) {
            entity.setad_element_id(this.targetId);
        }
    }

    public DbUser getUserById(Guid userId) {
        GetDbUserByUserIdParameters queryParameters = new GetDbUserByUserIdParameters(userId);
        VdcQueryReturnValue userQueryResponse = runQuery(VdcQueryType.GetDbUserByUserId, queryParameters);

        DbUser returnValue = null;
        if (userQueryResponse != null && userQueryResponse.getSucceeded()) {
            returnValue = (DbUser) userQueryResponse.getReturnValue();
        }

        return returnValue;
    }

    public Map<Guid, DbUser> getUsers() {
        HashMap<Guid, DbUser> users = new HashMap<Guid, DbUser>();
        for (DbUser user : lookupUsers()) {
            users.put(user.getuser_id(), user);
        }
        return users;
    }

    private List<DbUser> lookupUsers() {
        if (isFiltered()) {
            return getBackendCollection(DbUser.class, VdcQueryType.GetAllDbUsers, new VdcQueryParametersBase());
        }
        return asCollection(DbUser.class, getEntity(List.class, SearchType.DBUser, "users:"));
    }

    /**
     * injects user/group base on permission owner type
     * @param entity the permission to map
     * @param user the permission owner
     * @return permission
     */
    public Permission map(permissions entity, DbUser user) {
        Permission template = new Permission();
        if (entity.getad_element_id() != null && user != null) {
            if (isUser(user)) {
                template.setUser(new User());
                template.getUser().setId(entity.getad_element_id().toString());
            } else if (entity.getad_element_id() != null) {
                template.setGroup(new Group());
                template.getGroup().setId(entity.getad_element_id().toString());
            }
        }
        return map(entity, template);
    }

    private static boolean isUser(DbUser user) {
        return !user.getIsGroup();
    }

    /**
     * @pre completeness of "user|group.id" already validated if not
     * user sub-collection
     */
    protected PermissionsOperationsParametes getPrincipal(permissions entity, Permission permission) {
        PermissionsOperationsParametes ret = null;
        if (isUserSubCollection() || permission.isSetUser()) {
            VdcUser user = new VdcUser();
            user.setUserId(isUserSubCollection()
                           ? targetId
                           : asGuid(permission.getUser().getId()));
            user.setDomainControler(getCurrent().get(Principal.class).getDomain());
            ret = new PermissionsOperationsParametes(entity, user);
        } else if (isGroupSubCollection() || permission.isSetGroup()) {
            LdapGroup group = new LdapGroup();
            group.setid(isGroupSubCollection()
                        ? targetId
                        : asGuid(permission.getGroup().getId()));
            group.setdomain(getCurrent().get(Principal.class).getDomain());
            ret = new PermissionsOperationsParametes(entity, group);
        }
        return ret;
    }

    @Override
    public Permission addParents(Permission permission) {
        // REVISIT for entity-level permissions we need an isUser
        // flag on the permissions entity in order to distinguish
        // between the user and group cases
        if (isGroupSubCollection() && permission.isSetUser() && permission.getUser().isSetId()) {
            permission.setGroup(new Group());
            permission.getGroup().setId(permission.getUser().getId());
            permission.setUser(null);
        }
        return permission;
    }

    protected permissions getPermissionsTemplate(Permission perm) {
        permissions permission = new permissions();
        // allow the target Id to be implicit in the client-provided
        // representation
        if (isPrincipalSubCollection()) {
            permission.setad_element_id(targetId);
            permission.setObjectId(getMapper(Permission.class, Guid.class).map(perm, null));
        } else {
            if (perm.getUser()!=null) {
                permission.setad_element_id(asGuid(perm.getUser().getId()));
            } else { //if user is null, group is not null; this was validated before
                permission.setad_element_id(asGuid(perm.getGroup().getId()));
            }
            permission.setObjectId(targetId);
            permission.setObjectType(objectType);
        }
        return permission;
    }

    protected boolean isPrincipalSubCollection() {
        return isUserSubCollection() || isGroupSubCollection();
    }

    protected boolean isUserSubCollection() {
        return User.class.equals(suggestedParentType);
    }

    protected boolean isGroupSubCollection() {
        return Group.class.equals(suggestedParentType);
    }

    protected permissions getPermissions(String id) {
        return getEntity(permissions.class,
                         VdcQueryType.GetPermissionById,
                         new MultilevelAdministrationByPermissionIdParameters(new Guid(id)),
                         id);
    }

    @Override
    protected Permission doPopulate(Permission model, permissions entity) {
        return model;
    }
}
