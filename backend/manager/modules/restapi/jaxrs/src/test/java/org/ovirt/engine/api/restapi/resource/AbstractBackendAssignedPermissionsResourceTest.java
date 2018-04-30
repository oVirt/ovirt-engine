package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAssignedPermissionsResourceTest
        extends AbstractBackendCollectionResourceTest<
                        Permission,
                        org.ovirt.engine.core.common.businessentities.Permission,
                        BackendAssignedPermissionsResource> {

    private Guid targetId;
    private Class<? extends BaseResource> targetType;
    private QueryType queryType;
    private QueryParametersBase queryParams;
    private String principalParameterName;
    private String queryParameterName;

    public AbstractBackendAssignedPermissionsResourceTest(Guid targetId,
                                                          QueryType queryType,
                                                          QueryParametersBase queryParams,
                                                          Class<? extends BaseResource> suggestedParentType,
                                                          String principalParameterName,
                                                          String queryParameterName) {
        super(new BackendAssignedPermissionsResource(targetId,
                                                     queryType,
                                                     queryParams,
                                                     suggestedParentType),
              null,
              "");
        this.targetId = targetId;
        this.targetType = suggestedParentType;
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.principalParameterName = principalParameterName;
        this.queryParameterName = queryParameterName;
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testAddPermission() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddPermission,
                                  PermissionsOperationsParameters.class,
                                  new String[] { principalParameterName,
                                                 "Permission.AdElementId",
                                                 "Permission.ObjectId",
                                                 "Permission.RoleId" },
                                  new Object[] { GUIDS[1], chooseElementId(), chooseObjectId(), GUIDS[3] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetPermissionById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Permission model = getModel();

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Permission);
        verifyModel((Permission) response.getEntity(), 0);
    }

    /**
     * When adding a permission this method decides what should be the identifier of the user or group.
     */
    private Guid chooseElementId() {
        // If the resource being tested is the collection of permissions associated to a user or group then the
        // identifier is that of the user or group of that resource, otherwise we use a fixed identifier:
        if (targetType == User.class || targetType == Group.class) {
            return targetId;
        }
        return GUIDS[1];
    }

    /**
     * When adding a permission this method decides what should be the identifier of the object.
     */
    private Guid chooseObjectId() {
        // If the resource being tested is the collection of permissions associated to a user or group then we use a
        // fixed identifier, otherwise we have to use the identifier of the object corresponding to the resource:
        if (targetType != User.class && targetType != Group.class) {
            return targetId;
        }
        return GUIDS[2];
    }

    protected DbUser getUserByIdx(int idx) {
        DbUser user = new DbUser();
        user.setId(GUIDS[idx]);
        user.setFirstName(NAMES[idx]);
        user.setLoginName(NAMES[idx]);
        return user;
    }

    protected abstract Permission getModel();

    @Override
    protected List<Permission> getCollection() {
        return collection.list().getPermissions();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        List<org.ovirt.engine.core.common.businessentities.Permission> perms = setUpPermissions();
        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     perms,
                                     failure);
    }

    protected void setUpQueryExpectations(String query, Object failure, Guid adElementId) {
        assertEquals("", query);

        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     setUpPermissionsWithAdElementId(adElementId),
                                     failure);

    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Permission getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Permission permission =
                new org.ovirt.engine.core.common.businessentities.Permission();
        permission.setId(GUIDS[index]);
        permission.setAdElementId(GUIDS[1]);
        permission.setObjectId(GUIDS[2]);
        permission.setObjectType(VdcObjectType.StoragePool);
        permission.setRoleId(GUIDS[3]);
        return permission;
    }

    protected List<org.ovirt.engine.core.common.businessentities.Permission> setUpPermissions() {
        List<org.ovirt.engine.core.common.businessentities.Permission> perms = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            perms.add(getEntity(i));
        }
        return perms;
    }

    protected List<org.ovirt.engine.core.common.businessentities.Permission> setUpPermissionsWithAdElementId(
            Guid adElementId) {
        List<org.ovirt.engine.core.common.businessentities.Permission> perms = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            org.ovirt.engine.core.common.businessentities.Permission entity = getEntity(i);
            entity.setAdElementId(adElementId);
            perms.add(entity);
        }
        return perms;
    }

    @Override
    protected void verifyModel(Permission model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSetDataCenter());
        assertEquals(GUIDS[2].toString(), model.getDataCenter().getId());
        assertTrue(model.isSetRole());
        assertEquals(GUIDS[3].toString(), model.getRole().getId());
    }
}


