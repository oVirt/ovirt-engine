package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ovirt.engine.api.model.Permission;

public class PermissionMapperTest
        extends AbstractInvertibleMappingTest<
                        Permission,
                        org.ovirt.engine.core.common.businessentities.Permission,
                        org.ovirt.engine.core.common.businessentities.Permission> {

    public PermissionMapperTest() {
        super(
                Permission.class,
                org.ovirt.engine.core.common.businessentities.Permission.class,
                org.ovirt.engine.core.common.businessentities.Permission.class);
    }

    @Override
    protected void verify(Permission model, Permission transform) {
        assertNotNull(transform);
        assertTrue(transform.isSetId());
        assertEquals(model.getId(), transform.getId());
        assertTrue(transform.isSetRole());
        assertEquals(model.getRole().getId(), transform.getRole().getId());
        assertTrue(transform.isSetDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
    }

}

