package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Role;

public class BackendAssignedRoleResourceTest extends AbstractBackendRoleResourceTest {

    public BackendAssignedRoleResourceTest() {
        super(new BackendRoleResource(GUIDS[0].toString(), GUIDS[1]));
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isSetUser());
        assertTrue(model.getUser().isSetId());
        assertEquals(GUIDS[1].toString(), model.getUser().getId());
    }
}

