package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
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

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
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

