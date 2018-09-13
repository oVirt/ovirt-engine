package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;

public class PermitMapperTest extends AbstractInvertibleMappingTest<Permit, ActionGroup, ActionGroup> {

    public PermitMapperTest() {
        super(Permit.class, ActionGroup.class, ActionGroup.class);
    }

    @Override
    protected Permit postPopulate(Permit from) {
        ActionGroup actionGroup = ActionGroup.ACCESS_IMAGE_STORAGE;
        from.setId(Integer.toString(actionGroup.getId()));
        from.setName(actionGroup.name().toLowerCase());
        from.setAdministrative(actionGroup.getRoleType() == RoleType.ADMIN);
        return from;
    }

    @Override
    protected void verify(Permit model, Permit transform) {
        assertNotNull(transform);
        assertTrue(transform.isSetName());
        assertEquals(model.getName(), transform.getName());
        assertTrue(transform.isSetId());
        assertEquals(model.getId(), transform.getId());
        assertTrue(transform.isSetAdministrative());
        assertEquals(model.isAdministrative(), transform.isAdministrative());
    }
}
