package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.model.RoleType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class PermitMapperTest extends AbstractInvertibleMappingTest<Permit, ActionGroup, ActionGroup> {

    public PermitMapperTest() {
        super(Permit.class, ActionGroup.class, ActionGroup.class);
    }

    @Override
    protected Permit postPopulate(Permit from) {
        ActionGroup actionGroup = MappingTestHelper.shuffle(ActionGroup.class);
        from.setId(Integer.toString(actionGroup.getId()));
        PermitType permitType = PermitMapper.map(actionGroup, (PermitType)null);
        from.setName(permitType.value());
        from.setAdministrative(actionGroup.getRoleType().toString().equals(RoleType.ADMIN.toString()));
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
