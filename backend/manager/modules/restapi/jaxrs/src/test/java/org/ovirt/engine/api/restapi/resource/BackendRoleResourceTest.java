package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
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

