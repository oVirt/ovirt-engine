package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.compat.Guid;

public class VmIconDefaultDaoTest extends BaseGenericDaoTestCase<Guid, VmIconDefault, VmIconDefaultDao> {

    private static final Guid NEW_ID = new Guid("29df2e6a-d8bf-4025-8dec-f6a1d3e19131");
    private static final Guid ALPS_LARGE_ICON_ID = new Guid("32a41e14-8ec0-4638-8c34-a8e2841efc7e");
    private static final Guid NONEXISTING_ID = new Guid("96dcabdb-a88e-43bc-ad49-76bbf2c6758a");
    private static final Guid EXISTING_ID = new Guid("99276847-fb86-4fc4-8c16-593802ef7a4d");
    private static final VmIconDefault OTHER_OS_VM_ICON_DEFAULT = new VmIconDefault(
            new Guid("a9eda7a9-6a5e-4f52-8efc-033f39228fc8"),
            0,
            FixturesTool.SMALL_ICON_ID,
            FixturesTool.LARGE_ICON_ID);

    @Override
    protected VmIconDefault generateNewEntity() {
        return new VmIconDefault(NEW_ID, 2, FixturesTool.SMALL_ICON_ID, FixturesTool.LARGE_ICON_ID);
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setLargeIconId(ALPS_LARGE_ICON_ID);
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return NONEXISTING_ID;
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 2;
    }

    @Test
    public void testRemoveAll() {
        dao.removeAll();
        final List<VmIconDefault> result = dao.getAll();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByLargeIconId() {
        final List<VmIconDefault> result = dao.getByLargeIconId(FixturesTool.LARGE_ICON_ID);
        assertTrue(result.contains(OTHER_OS_VM_ICON_DEFAULT));
    }

    @Test
    public void testGetByOperatingSystemIdExisting() {
        final VmIconDefault vmIconDefault = dao.getByOperatingSystemId(OTHER_OS_VM_ICON_DEFAULT.getOsId());
        assertEquals(OTHER_OS_VM_ICON_DEFAULT, vmIconDefault);
    }

    @Test
    public void testGetByOperatingSystemIdNonExisting() {
        final VmIconDefault vmIconDefault = dao.getByOperatingSystemId(-1);
        assertNull(vmIconDefault);
    }
}
