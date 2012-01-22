package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;

/**
 * Unit tests to validate {@link VmDeviceDao}.
 */
public class VmDeviceDAOTest extends BaseGenericDaoTestCase<VmDeviceId, VmDevice, VmDeviceDAO> {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_DEVICE_ID = new Guid("e14ed6f0-3b12-11e1-b614-63d00126418d");
    private static final int TOTAL_DEVICES = 1;

    @Override
    protected VmDeviceId generateNonExistingId() {
        return new VmDeviceId(Guid.NewGuid(), Guid.NewGuid());
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DEVICES;
    }

    @Override
    protected VmDevice generateNewEntity() {
        return new VmDevice(new VmDeviceId(Guid.NewGuid(), EXISTING_VM_ID),
                "disk",
                "floppy",
                "type:'drive', controller:'0', bus:'0', unit:'1'",
                2,
                "",
                true, false, false, false);
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setAddress("type:'drive', controller:'0', bus:'0', unit:'0'");
    }

    @Override
    protected VmDeviceDAO prepareDao() {
        return prepareDAO(dbFacade.getVmDeviceDAO());
    }

    @Override
    protected VmDeviceId getExistingEntityId() {
        return (new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID));
    }

    @Test
    public void existsForExistingVmDevice() throws Exception {
        assertTrue(dao.exists(new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID)));
    }

    @Test
    public void existsForNonExistingVmDevice() throws Exception {
        assertFalse(dao.exists(new VmDeviceId(Guid.NewGuid(), Guid.NewGuid())));
    }
}
