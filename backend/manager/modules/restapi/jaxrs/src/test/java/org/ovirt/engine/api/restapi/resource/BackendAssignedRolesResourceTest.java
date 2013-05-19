package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAssignedRolesResourceTest
        extends AbstractBackendCollectionResourceTest<Role, permissions, BackendAssignedRolesResource> {

    public BackendAssignedRolesResourceTest() {
        super(new BackendAssignedRolesResource(GUIDS[0]),null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(GUIDS[1], false);
        setUpGetEntityExpectations(VdcQueryType.GetPermissionsByAdElementId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   setUpPermissions());
        setUriInfo(setUpActionExpectations(VdcActionType.RemovePermission,
                                           PermissionsOperationsParametes.class,
                                           new String[] { "Permission.ad_element_id", "Permission.role_id" },
                                           new Object[] { GUIDS[0], GUIDS[1] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[1].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(NON_EXISTANT_GUID, true);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(wae.getResponse().getStatus(), 404);
        }
    }

    private void setUpGetEntityExpectations(Guid entityId, Boolean returnNull) throws Exception {
        org.ovirt.engine.core.common.businessentities.Role role = null;
        if (!returnNull) {
            role = new org.ovirt.engine.core.common.businessentities.Role();
            role.setId(entityId);
        }
        setUpGetEntityExpectations(VdcQueryType.GetRoleById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entityId },
                role);
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(GUIDS[1], false);
        setUpGetEntityExpectations(VdcQueryType.GetPermissionsByAdElementId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   setUpPermissions());
        setUriInfo(setUpActionExpectations(VdcActionType.RemovePermission,
                                           PermissionsOperationsParametes.class,
                                           new String[] { "Permission.ad_element_id", "Permission.role_id" },
                                           new Object[] { GUIDS[0], GUIDS[1] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[1].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddAssignedRole() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddSystemPermission,
                                  PermissionsOperationsParametes.class,
                                  new String[] { "Permission.ad_element_id", "Permission.role_id" },
                                  new Object[] { GUIDS[0], GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetPermissionById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(1));
        Role model = new Role();
        model.setId(GUIDS[1].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Role);
        verifyModel((Role) response.getEntity(), 1);
    }

    @Test
    public void testAddAssignedRoleByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetRoleByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                getRole());
        setUpCreationExpectations(VdcActionType.AddSystemPermission,
                                  PermissionsOperationsParametes.class,
                                  new String[] { "Permission.ad_element_id", "Permission.role_id" },
                                  new Object[] { GUIDS[0], GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetPermissionById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(1));
        Role model = new Role();
        model.setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Role);
        verifyModel((Role) response.getEntity(), 1);
    }

    private org.ovirt.engine.core.common.businessentities.Role getRole() {
        org.ovirt.engine.core.common.businessentities.Role role = new org.ovirt.engine.core.common.businessentities.Role();
        role.setId(GUIDS[1]);
        return role;
    }

    @Override
    protected List<Role> getCollection() {
        return collection.list().getRoles();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetPermissionsByAdElementId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     setUpPermissions(),
                                     failure);

        control.replay();
    }

    @Override
    protected permissions getEntity(int index) {
        permissions permission = new permissions();
        permission.setId(GUIDS[(index + 1) % 3]);
        permission.setad_element_id(GUIDS[0]);
        permission.setrole_id(GUIDS[index]);
        permission.setObjectType(VdcObjectType.System);
        return permission;
    }

    protected List<permissions> setUpPermissions() {
        List<permissions> perms = new ArrayList<permissions>();
        for (int i = 0; i < NAMES.length; i++) {
            perms.add(getEntity(i));
        }
        return perms;
    }

    @Override
    protected void verifyModel(Role model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
    }
}

