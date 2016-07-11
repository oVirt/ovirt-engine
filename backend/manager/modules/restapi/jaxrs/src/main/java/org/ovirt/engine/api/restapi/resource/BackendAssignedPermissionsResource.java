package org.ovirt.engine.api.restapi.resource;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.api.restapi.types.GroupMapper;
import org.ovirt.engine.api.restapi.types.UserMapper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAssignedPermissionsResource
        extends AbstractBackendCollectionResource<Permission, org.ovirt.engine.core.common.businessentities.Permission>
        implements AssignedPermissionsResource {

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
        super(Permission.class, org.ovirt.engine.core.common.businessentities.Permission.class);
        this.targetId = targetId;
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.suggestedParentType = suggestedParentType;
        this.objectType = objectType;
    }

    @Override
    public Permissions list() {
        Set<org.ovirt.engine.core.common.businessentities.Permission> permissions =
                new TreeSet<>(new PermissionsComparator());
        List<org.ovirt.engine.core.common.businessentities.Permission> directPermissions =
                getBackendCollection(queryType, queryParams);
        permissions.addAll(directPermissions);
        if (queryType.equals(VdcQueryType.GetPermissionsForObject)) {
            permissions.addAll(getInheritedPermissions());
        }
        return mapCollection(permissions);
    }

    private List<org.ovirt.engine.core.common.businessentities.Permission> getInheritedPermissions() {
        ((GetPermissionsForObjectParameters)queryParams).setVdcObjectType(objectType);
        ((GetPermissionsForObjectParameters)queryParams).setDirectOnly(false);
        List<org.ovirt.engine.core.common.businessentities.Permission> inheritedPermissions =
                getBackendCollection(queryType, queryParams);
        for (org.ovirt.engine.core.common.businessentities.Permission entity : inheritedPermissions) {
            if (objectType != null) {
                entity.setObjectType(objectType);
                entity.setObjectId(targetId);
            }
        }
        return inheritedPermissions;
    }

    static class PermissionsComparator
            implements Comparator<org.ovirt.engine.core.common.businessentities.Permission>, Serializable {
        @Override
        public int compare(
                org.ovirt.engine.core.common.businessentities.Permission o1,
                org.ovirt.engine.core.common.businessentities.Permission o2) {
            String id1 = o1.getId().toString();
            String id2 = o2.getId().toString();
            return id1.compareTo(id2);
        }
    }

    @Override
    public Response add(Permission permission) {
        validateParameters(permission,
                           isPrincipalSubCollection()
                           ? new String[] {"role.id|name", "dataCenter|cluster|host|storageDomain|vm|vmPool|template.id"}
                           : new String[] {"role.id|name", "user|group.id"});
        PermissionsOperationsParameters parameters = getParameters(permission);
        QueryIdResolver<Guid> resolver = new QueryIdResolver<>(VdcQueryType.GetPermissionById, IdQueryParameters.class);
        return performCreate(VdcActionType.AddPermission, parameters, resolver);
    }

    @Override
    public PermissionResource getPermissionResource(String id) {
        return inject(new BackendPermissionResource(id, targetId, this, suggestedParentType));
    }

    protected Permissions mapCollection(Set<org.ovirt.engine.core.common.businessentities.Permission> entities) {
        Permissions collection = new Permissions();
        for (org.ovirt.engine.core.common.businessentities.Permission entity : entities) {
             castEveryonePermissionsToUser(entity);
             Permission permission = map(entity, getUserById(entity.getAdElementId()));
             collection.getPermissions().add(addLinks(permission, permission.getUser() != null ? suggestedParentType : Group.class));
        }
        return collection;
    }

    private void castEveryonePermissionsToUser(org.ovirt.engine.core.common.businessentities.Permission entity) {
        if (entity.getAdElementId() != null &&
            entity.getAdElementId().equals(Guid.EVERYONE) &&
            queryType.equals(VdcQueryType.GetPermissionsByAdElementId)) {
            entity.setAdElementId(this.targetId);
        }
    }

    public DbUser getUserById(Guid userId) {
        IdQueryParameters queryParameters = new IdQueryParameters(userId);
        VdcQueryReturnValue userQueryResponse = runQuery(VdcQueryType.GetDbUserByUserId, queryParameters);

        DbUser returnValue = null;
        if (userQueryResponse != null && userQueryResponse.getSucceeded()) {
            returnValue = userQueryResponse.getReturnValue();
        }

        return returnValue;
    }

    public Map<Guid, DbUser> getUsers() {
        HashMap<Guid, DbUser> users = new HashMap<>();
        for (DbUser user : lookupUsers()) {
            users.put(user.getId(), user);
        }
        return users;
    }

    private List<DbUser> lookupUsers() {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setFiltered(isFiltered());
        return getBackendCollection(DbUser.class, VdcQueryType.GetAllDbUsers, queryParams);
    }

    /**
     * injects user/group base on permission owner type
     * @param entity the permission to map
     * @param user the permission owner
     * @return permission
     */
    public Permission map(org.ovirt.engine.core.common.businessentities.Permission entity, DbUser user) {
        Permission template = new Permission();
        if (entity.getAdElementId() != null) {
            if (isUser(user)) {
                template.setUser(new User());
                template.getUser().setId(entity.getAdElementId().toString());
            } else {
                template.setGroup(new Group());
                template.getGroup().setId(entity.getAdElementId().toString());
            }
        }
        return map(entity, template);
    }

    //REVISIT: fix once BE can distinguish between the user and group
    private static boolean isUser(DbUser user) {
        return user != null && !user.isGroup();
    }

    /**
     * Find the user or group that the permissions applies to.
     *
     * @param permission the incoming permission model
     * @return the user or group that the permission applies to
     */
    private Object getPrincipal(Permission permission) {
        if (isUserSubCollection()) {
            DbUser dbUser = new DbUser();
            dbUser.setId(targetId);
            return dbUser;
        }
        if (isGroupSubCollection()) {
            DbGroup dbGroup = new DbGroup();
            dbGroup.setId(targetId);
            return dbGroup;
        }
        if (permission.isSetUser()) {
            User user = permission.getUser();
            DbUser dbUser = UserMapper.map(user, null);
            if (dbUser.getDomain() == null) {
                dbUser.setDomain(getCurrent().getUser().getDomain());
            }
            return dbUser;
        }
        if (permission.isSetGroup()) {
            Group group = permission.getGroup();
            DbGroup dbGroup = GroupMapper.map(group, null);
            if (dbGroup.getDomain() == null) {
                dbGroup.setDomain(getCurrent().getUser().getDomain());
            }
            return dbGroup;
        }
        return null;
    }

    /**
     * Create the parameters for the permissions operation.
     *
     * @param model the incoming permission
     * @return the parameters for the operation
     */
    private PermissionsOperationsParameters getParameters(Permission model) {
        org.ovirt.engine.core.common.businessentities.Permission entity = map(model, null);
        if (!isPrincipalSubCollection()) {
            entity.setObjectId(targetId);
            entity.setObjectType(objectType);
        }
        PermissionsOperationsParameters parameters = new PermissionsOperationsParameters();
        parameters.setPermission(entity);
        Object principal = getPrincipal(model);
        if (principal instanceof DbUser) {
            DbUser user = (DbUser) principal;
            entity.setAdElementId(user.getId());
            parameters.setUser(user);
        }
        if (principal instanceof DbGroup) {
            DbGroup group = (DbGroup) principal;
            entity.setAdElementId(group.getId());
            parameters.setGroup(group);
        }
        return parameters;
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

    protected boolean isPrincipalSubCollection() {
        return isUserSubCollection() || isGroupSubCollection();
    }

    protected boolean isUserSubCollection() {
        return User.class.equals(suggestedParentType);
    }

    protected boolean isGroupSubCollection() {
        return Group.class.equals(suggestedParentType);
    }
}
