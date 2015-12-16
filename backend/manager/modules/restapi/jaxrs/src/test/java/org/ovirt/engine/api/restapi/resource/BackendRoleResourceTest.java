package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendRoleResourceTest extends AbstractBackendRoleResourceTest {

    public BackendRoleResourceTest() {
        super(new BackendRoleResource(GUIDS[0].toString()));
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertFalse(model.isSetUser());
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateRole,
                                           RolesOperationsParameters.class,
                                           new String[] { "RoleId", "Role" },
                                           new Object[] { GUIDS[0], getEntity(0) },
                                           true,
                                           true));

        verifyModel(resource.update(getModel()), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveRole,
                                           RolesParameterBase.class,
                                           new String[] { "RoleId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetRoleById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveRole,
                                           RolesParameterBase.class,
                                           new String[] { "RoleId" },
                                           new Object[] { GUIDS[0] },
                                           valid,
                                           success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }


    private Role getModel() {
        Role role = new Role();
        role.setName(NAMES[0]);
        return role;
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        for (int i=0; i<times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetRoleById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       getEntity(0));
        }
    }
}

