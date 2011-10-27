package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Role;

public class BackendRoleResourceTest extends AbstractBackendRoleResourceTest {

    public BackendRoleResourceTest() {
        super(new BackendRoleResource(GUIDS[0].toString()));
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertFalse(model.isSetUser());
    }
}

