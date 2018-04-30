package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Role;

public class RoleMapperTest extends AbstractInvertibleMappingTest<Role, org.ovirt.engine.core.common.businessentities.Role, org.ovirt.engine.core.common.businessentities.Role> {

    public RoleMapperTest() {
        super(Role.class, org.ovirt.engine.core.common.businessentities.Role.class, org.ovirt.engine.core.common.businessentities.Role.class);
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

