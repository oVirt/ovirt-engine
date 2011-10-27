package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.businessentities.roles;

public class RoleMapperTest extends AbstractInvertibleMappingTest<Role, roles, roles> {

    public RoleMapperTest() {
        super(Role.class, roles.class, roles.class);
    }

    @Override
    protected void verify(Role model, Role transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.isMutable(), transform.isMutable());
        assertEquals(model.isAdministrative(), transform.isAdministrative());
    }
}

