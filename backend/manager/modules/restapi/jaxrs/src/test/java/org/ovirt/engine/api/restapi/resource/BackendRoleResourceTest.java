package org.ovirt.engine.api.restapi.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

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
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(ActionType.UpdateRole,
                                           RolesOperationsParameters.class,
                                           new String[] { "RoleId", "Role" },
                                           new Object[] { GUIDS[0], getEntity(0) },
                                           true,
                                           true));

        verifyModel(resource.update(getModel()), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(ActionType.RemoveRole,
                                           RolesParameterBase.class,
                                           new String[] { "RoleId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(QueryType.GetRoleById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(ActionType.RemoveRole,
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

    protected void setUpGetEntityExpectations(int times) {
        for (int i=0; i<times; i++) {
            setUpGetEntityExpectations(QueryType.GetRoleById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       getEntity(0));
        }
    }
}

