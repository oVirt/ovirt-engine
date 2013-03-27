package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Unit tests to validate {@link VmDeviceDao}.
 */
public class VmDeviceDAOTest extends BaseGenericDaoTestCase<VmDeviceId, VmDevice, VmDeviceDAO> {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_DEVICE_ID = new Guid("e14ed6f0-3b12-11e1-b614-63d00126418d");
    private static final int TOTAL_DEVICES = 10;
    private static final int TOTAL_DEVICES_FOR_EXISTING_VM = 3;


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
                new HashMap<String, Object>(),
                true, false, false, "alias");
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setAddress("type:'drive', controller:'0', bus:'0', unit:'0'");
    }

    @Override
    protected VmDeviceDAO prepareDao() {
        return dbFacade.getVmDeviceDao();
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

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceNoFiltering() {
        List<VmDevice> devices = dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, "disk", "disk");
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringSetToFlase() {
        List<VmDevice> devices = dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, "disk", "disk", null, false);
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, "disk", "disk", PRIVILEGED_USER_ID, true);
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithoutPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, "disk", "disk", UNPRIVILEGED_USER_ID, true);
        assertTrue("A user without any permissions should not see any devices", devices.isEmpty());
    }

    /**
     * Asserts all the devices are present in a result of {@link VmDeviceDAO#getVmDeviceByVmIdTypeAndDevice(Guid, String, String)
     * @param devices The result to check
     */
    private static void assertGetVMDeviceByIdTypeAndDeviceFullResult(List<VmDevice> devices) {
        assertEquals("there should only be " + TOTAL_DEVICES_FOR_EXISTING_VM + " disks", TOTAL_DEVICES_FOR_EXISTING_VM, devices.size());
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithPermissionsNoFiltering() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, "disk", "disk", PRIVILEGED_USER_ID, false);
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetUnmanagedDeviceByVmId() {
        List<VmDevice> devices =
                dao.getUnmanagedDevicesByVmId(EXISTING_VM_ID);
        assertTrue(devices.isEmpty());
    }

    @Test
    public void testIsBalloonEnabled() {
        boolean queryRes = dao.isMemBalloonEnabled(EXISTING_VM_ID);
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID,
                        VmDeviceType.BALLOON.getName(),
                        VmDeviceType.MEMBALLOON.getName());
        assertTrue((queryRes && !devices.isEmpty()) || (!queryRes && devices.isEmpty()));
    }

    /**
     * Test clearing a device address
     * @param result
     */
    @Test
    public void clearDeviceAddress() {
        // before: check we have a device with a non-blank address
        VmDevice vmDevice = dao.get(getExistingEntityId());
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));

        // clear the address and check its really cleared
        dao.clearDeviceAddress(getExistingEntityId().getDeviceId());
        Assert.assertTrue(StringUtils.isBlank(dao.get(getExistingEntityId()).getAddress()));

    }

    @Test
    public void testUpdateDeviceRuntimeInfo() {
        VmDevice vmDevice = dao.get(getExistingEntityId());
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));
        String newAddressValue = "newaddr";
        vmDevice.setAddress(newAddressValue);
        String newAlias = "newalias";
        vmDevice.setAlias(newAlias);
        dao.updateRuntimeInfo(vmDevice);
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.getAddress(), newAddressValue);
        assertEquals(vmDevice.getAlias(), newAlias);
    }

    @Test
    public void testUpdateHotPlugDisk() {
        VmDevice vmDevice = dao.get(getExistingEntityId());
        boolean newPluggedValue = !vmDevice.getIsPlugged();
        int newBootOrderValue = vmDevice.getBootOrder() + 1;
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));
        vmDevice.setBootOrder(newBootOrderValue);
        vmDevice.setIsPlugged(newPluggedValue);
        dao.updateHotPlugDisk(vmDevice);
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.getIsPlugged(), newPluggedValue);
        assertEquals(vmDevice.getBootOrder(),newBootOrderValue);
    }

}
