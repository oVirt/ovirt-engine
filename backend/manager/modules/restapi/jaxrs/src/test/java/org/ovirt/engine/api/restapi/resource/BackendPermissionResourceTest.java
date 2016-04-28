package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendPermissionResourceTest
        extends AbstractBackendSubResourceTest<
                        Permission,
                        org.ovirt.engine.core.common.businessentities.Permission,
                        BackendPermissionResource> {

    public BackendPermissionResourceTest() {
        super(new BackendPermissionResource(GUIDS[0].toString(),
                                            GUIDS[1],
                                            new BackendAssignedPermissionsResource(GUIDS[0],
                                                                                   VdcQueryType.GetPermissionsForObject,
                                                                                   new GetPermissionsForObjectParameters(GUIDS[0]),
                                                                                   Cluster.class,
                                                                                   VdcObjectType.Cluster),
                                            User.class));
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        initResource(resource.parent);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendPermissionResource("foo", null, null, null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(VdcQueryType.GetAllDbUsers,
                VdcQueryParametersBase.class,
                new String[] { "Refresh", "Filtered" },
                new Object[] { true, false },
                getUsers());

        setUpGetEntityExpectations(1);

        control.replay();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(2);
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllDbUsers,
            VdcQueryParametersBase.class,
            new String[] { "Refresh", "Filtered" },
            new Object[] { true, false },
            getUsers()
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemovePermission,
                PermissionsOperationsParameters.class,
                new String[] { "Permission.Id" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllDbUsers,
            VdcQueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getUsers()
        );
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllDbUsers,
            VdcQueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getUsers()
        );
        doTestBadRemove(true, false, FAILURE);
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemovePermission,
                PermissionsOperationsParameters.class,
                new String[] { "Permission.Id" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(
                VdcQueryType.GetPermissionById,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                notFound ? null : getEntity(0)
            );
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Permission getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Permission permission =
                new org.ovirt.engine.core.common.businessentities.Permission();
        permission.setId(GUIDS[0]);
        permission.setAdElementId(GUIDS[1]);
        permission.setRoleId(GUIDS[2]);
        return permission;
    }

    @Override
    protected void verifyModel(Permission model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSetUser());
        assertEquals(GUIDS[1].toString(), model.getUser().getId());
        assertTrue(model.isSetRole());
        assertEquals(GUIDS[2].toString(), model.getRole().getId());
    }

    protected ArrayList<DbUser> getUsers() {
        ArrayList<DbUser> users = new ArrayList<>();
        for (int i=0; i < NAMES.length; i++) {
            DbUser user = new DbUser();
            user.setId(GUIDS[i]);
            user.setFirstName(NAMES[i]);
            user.setLoginName(NAMES[i]);
            users.add(user);
        }
        return users;
    }
}

