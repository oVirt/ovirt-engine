package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendPermissionResourceTest
        extends AbstractBackendSubResourceTest<Permission, Permissions, BackendPermissionResource> {

    public BackendPermissionResourceTest() {
        super(new BackendPermissionResource(GUIDS[0].toString(),
                                            new BackendAssignedPermissionsResource(GUIDS[0],
                                                                                   VdcQueryType.GetPermissionsForObject,
                                                                                   new GetPermissionsForObjectParameters(GUIDS[0]),
                                                                                   Cluster.class,
                                                                                   VdcObjectType.VdsGroups),
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
            new BackendPermissionResource("foo", null, null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
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

        setUpGetEntityExpectations();

        control.replay();
        verifyModel(resource.get(), 0);
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetPermissionById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   notFound ? null : getEntity(0));
    }

    @Override
    protected Permissions getEntity(int index) {
        Permissions permission = new Permissions();
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
        ArrayList<DbUser> users = new ArrayList<DbUser>();
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

