package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAssignedPermissionsResourceTest
        extends AbstractBackendCollectionResourceTest<Permission, Permissions, BackendAssignedPermissionsResource> {

    private Guid targetId;
    private Class<? extends BaseResource> targetType;
    private VdcQueryType queryType;
    private VdcQueryParametersBase queryParams;
    private String principalParameterName;
    private String queryParameterName;

    public AbstractBackendAssignedPermissionsResourceTest(Guid targetId,
                                                          VdcQueryType queryType,
                                                          VdcQueryParametersBase queryParams,
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
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(2, GUIDS[0], getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetAllDbUsers,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getUsers());

        setUriInfo(setUpActionExpectations(VdcActionType.RemovePermission,
                                           PermissionsOperationsParameters.class,
                                           new String[] { "Permission.Id" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetAllDbUsers,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getUsers());

        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetAllDbUsers,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getUsers());

        doTestBadRemove(true, false, FAILURE);
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(1, NON_EXISTANT_GUID, null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(wae.getResponse().getStatus(), 404);
        }
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(2, GUIDS[0], getEntity(0));
        setUriInfo(setUpActionExpectations(VdcActionType.RemovePermission,
                                           PermissionsOperationsParameters.class,
                                           new String[] { "Permission.Id" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddPermission() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddPermission,
                                  PermissionsOperationsParameters.class,
                                  new String[] { principalParameterName,
                                                 "Permission.ad_element_id",
                                                 "Permission.ObjectId",
                                                 "Permission.role_id" },
                                  new Object[] { GUIDS[1], chooseElementId(), chooseObjectId(), GUIDS[3] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetPermissionById,
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

    protected ArrayList<DbUser> getUsers() {
        ArrayList<DbUser> users = new ArrayList<DbUser>();
        for (int i=0; i < NAMES.length; i++) {
            users.add(getUserByIdx(i));
        }
        return users;
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
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        List<Permissions> perms = setUpPermissions();
        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     perms,
                                     failure);

        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure, Guid adElementId) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     setUpPermissionsWithAdElementId(adElementId),
                                     failure);

        control.replay();
    }

    protected void setUpGetEntityExpectations(int times, Guid entityId, Permissions permission) throws Exception {
        while (times-->0) {
            setUpGetEntityExpectations(VdcQueryType.GetPermissionById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { entityId },
                                       permission);
        }
    }

    @Override
    protected Permissions getEntity(int index) {
        Permissions permission = new Permissions();
        permission.setId(GUIDS[index]);
        permission.setad_element_id(GUIDS[1]);
        permission.setObjectId(GUIDS[2]);
        permission.setObjectType(VdcObjectType.StoragePool);
        permission.setrole_id(GUIDS[3]);
        return permission;
    }

    protected List<Permissions> setUpPermissions() {
        List<Permissions> perms = new ArrayList<Permissions>();
        for (int i = 0; i < NAMES.length; i++) {
            perms.add(getEntity(i));
        }
        return perms;
    }

    protected List<Permissions> setUpPermissionsWithAdElementId(Guid adElementId) {
        List<Permissions> perms = new ArrayList<Permissions>();
        for (int i = 0; i < NAMES.length; i++) {
            Permissions entity = getEntity(i);
            entity.setad_element_id(adElementId);
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


