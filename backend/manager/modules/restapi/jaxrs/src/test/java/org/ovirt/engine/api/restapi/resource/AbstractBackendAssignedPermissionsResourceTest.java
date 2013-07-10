package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAssignedPermissionsResourceTest
        extends AbstractBackendCollectionResourceTest<Permission, permissions, BackendAssignedPermissionsResource> {

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
        setUpGetEntityExpectations(VdcQueryType.Search,
                                   SearchParameters.class,
                                   new String[] {"SearchPattern", "SearchTypeValue"},
                                   new Object[] {"users:", SearchType.DBUser},
                                   getUsers());
        setUriInfo(setUpActionExpectations(VdcActionType.RemovePermission,
                                           PermissionsOperationsParametes.class,
                                           new String[] { "Permission.Id" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.Search,
                                   SearchParameters.class,
                                   new String[] {"SearchPattern", "SearchTypeValue"},
                                   new Object[] {"users:", SearchType.DBUser},
                                   getUsers());
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.Search,
                                   SearchParameters.class,
                                   new String[] {"SearchPattern", "SearchTypeValue"},
                                   new Object[] {"users:", SearchType.DBUser},
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
                                           PermissionsOperationsParametes.class,
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
                                  PermissionsOperationsParametes.class,
                                  new String[] { principalParameterName,
                                                 "Permission.ad_element_id",
                                                 "Permission.ObjectId",
                                                 "Permission.role_id" },
                                  new Object[] { GUIDS[1], GUIDS[1], GUIDS[2], GUIDS[3] },
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
        assert(query.equals(""));

        List<permissions> perms = setUpPermissions();
        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     perms,
                                     failure);

        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure, Guid adElementId) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(queryType,
                                     queryParams.getClass(),
                                     new String[] { queryParameterName },
                                     new Object[] { GUIDS[1] },
                                     setUpPermissionsWithAdElementId(adElementId),
                                     failure);

        control.replay();
    }

    protected void setUpGetEntityExpectations(int times, Guid entityId, permissions permission) throws Exception {
        while (times-->0) {
            setUpGetEntityExpectations(VdcQueryType.GetPermissionById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { entityId },
                                       permission);
        }
    }

    @Override
    protected permissions getEntity(int index) {
        permissions permission = new permissions();
        permission.setId(GUIDS[index]);
        permission.setad_element_id(GUIDS[1]);
        permission.setObjectId(GUIDS[2]);
        permission.setObjectType(VdcObjectType.StoragePool);
        permission.setrole_id(GUIDS[3]);
        return permission;
    }

    protected List<permissions> setUpPermissions() {
        List<permissions> perms = new ArrayList<permissions>();
        for (int i = 0; i < NAMES.length; i++) {
            perms.add(getEntity(i));
        }
        return perms;
    }

    protected List<permissions> setUpPermissionsWithAdElementId(Guid adElementId) {
        List<permissions> perms = new ArrayList<permissions>();
        for (int i = 0; i < NAMES.length; i++) {
            permissions entity = getEntity(i);
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


