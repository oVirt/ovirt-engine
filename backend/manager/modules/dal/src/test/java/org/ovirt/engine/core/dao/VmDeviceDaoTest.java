package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Unit tests to validate {@link VmDeviceDao}.
 */
public class VmDeviceDaoTest extends BaseGenericDaoTestCase<VmDeviceId, VmDevice, VmDeviceDao> {

    private static final Guid EXISTING_VM_ID = FixturesTool.VM_RHEL5_POOL_57;
    private static final Guid EXISTING_VM_ID_2 = FixturesTool.VM_TEMPLATE_RHEL5;
    private static final Guid EXISTING_VM_ID_3 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5002");
    private static final Guid EXISTING_DEVICE_ID = new Guid("e14ed6f0-3b12-11e1-b614-63d00126418d");
    private static final Guid NON_EXISTING_VM_ID = Guid.newGuid();
    private static final int TOTAL_DEVICES = 19;
    private static final int TOTAL_DISK_DEVICES_FOR_EXISTING_VM = 5;
    private static final int TOTAL_HOST_DEVICES = 3;
    private static final int TOTAL_DEVICES_FOR_EXISTING_VM = 8;

    @Override
    protected VmDeviceId generateNonExistingId() {
        return new VmDeviceId(Guid.newGuid(), Guid.newGuid());
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_DEVICES;
    }

    @Override
    protected VmDevice generateNewEntity() {
        return createVmDevice(EXISTING_VM_ID);
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setAddress("type:'drive', controller:'0', bus:'0', unit:'0'");
        existingEntity.setLogicalName("testLogicalName");
    }

    @Override
    protected VmDeviceId getExistingEntityId() {
        return new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID);
    }

    @Test
    public void existsForExistingVmDevice() {
        assertTrue(dao.exists(new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID)));
    }

    @Test
    public void existsForNonExistingVmDevice() {
        assertFalse(dao.exists(new VmDeviceId(Guid.newGuid(), Guid.newGuid())));
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceNoFiltering() {
        List<VmDevice> devices = dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, VmDeviceGeneralType.DISK, "disk");
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringSetToFalse() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID, VmDeviceGeneralType.DISK, "disk", null, false);
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID,
                        VmDeviceGeneralType.DISK,
                        "disk",
                        PRIVILEGED_USER_ID,
                        true);
        assertGetVMDeviceByIdTypeAndDeviceFullResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithoutPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID,
                        VmDeviceGeneralType.DISK,
                        "disk",
                        UNPRIVILEGED_USER_ID,
                        true);
        assertTrue(devices.isEmpty(), "A user without any permissions should not see any devices");
    }

    /**
     * Asserts all the disk devices are present in a result of
     * {@link VmDeviceDao#getVmDeviceByVmIdTypeAndDevice(Guid, VmDeviceGeneralType, String)}.
     * @param devices The result to check
     */
    private static void assertGetVMDeviceByIdTypeAndDeviceFullResult(List<VmDevice> devices) {
        assertEquals(TOTAL_DISK_DEVICES_FOR_EXISTING_VM, devices.size(),
                "there should only be " + TOTAL_DISK_DEVICES_FOR_EXISTING_VM + " disks");
    }

    /**
     * Asserts all the devices are present in a result of
     * {@link VmDeviceDao#getVmDeviceByVmIdTypeAndDevice(Guid, VmDeviceGeneralType, String)}.
     * @param devices The result to check
     */
    private static void assertGetVMDeviceByIdResult(List<VmDevice> devices) {
        assertEquals(TOTAL_DEVICES_FOR_EXISTING_VM, devices.size(),
                "there should only be " + TOTAL_DEVICES_FOR_EXISTING_VM + " devices");
    }

    @Test
    public void testGetVmDeviceByVmIdTypeAndDeviceFilteringWithPermissionsNoFiltering() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmIdTypeAndDevice(EXISTING_VM_ID,
                        VmDeviceGeneralType.DISK,
                        "disk",
                        PRIVILEGED_USER_ID,
                        false);
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
                        VmDeviceGeneralType.BALLOON,
                        VmDeviceType.MEMBALLOON.getName());
        assertTrue((queryRes && !devices.isEmpty()) || (!queryRes && devices.isEmpty()));
    }

    /**
     * Test clearing a device address
     */
    @Test
    public void clearDeviceAddress() {
        // before: check we have a device with a non-blank address
        VmDevice vmDevice = dao.get(getExistingEntityId());
        assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));

        // clear the address and check its really cleared
        dao.clearDeviceAddress(getExistingEntityId().getDeviceId());
        assertTrue(StringUtils.isBlank(dao.get(getExistingEntityId()).getAddress()));

    }

    @Test
    public void testExistsVmDeviceByVmIdAndType() {
        assertTrue(dao.existsVmDeviceByVmIdAndType(EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV));
        assertFalse(dao.existsVmDeviceByVmIdAndType(NON_EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV));
    }

    @Test
    public void testGetVmDeviceByType() {
        List<VmDevice> devices = dao.getVmDeviceByType(VmDeviceGeneralType.HOSTDEV);
        assertEquals(TOTAL_HOST_DEVICES, devices.size(), "Expected to retrieve " + TOTAL_HOST_DEVICES + " host devices.");
        Set<Guid> vmIds = devices.stream().map(VmDevice::getVmId).collect(Collectors.toSet());
        assertTrue(vmIds.contains(EXISTING_VM_ID));
        assertTrue(vmIds.contains(EXISTING_VM_ID_2));
        assertTrue(vmIds.contains(EXISTING_VM_ID_3));
    }

    @Test
    public void testRemoveVmDevicesByVmIdAndType() {
        dao.removeVmDevicesByVmIdAndType(EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV);
        assertFalse(dao.existsVmDeviceByVmIdAndType(EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV));
        assertTrue(dao.existsVmDeviceByVmIdAndType(EXISTING_VM_ID_2, VmDeviceGeneralType.HOSTDEV));
    }

    private VmDevice createVmDevice(Guid vmGuid) {
        return new VmDevice(new VmDeviceId(Guid.newGuid(), vmGuid),
                VmDeviceGeneralType.DISK,
                "floppy",
                "type:'drive', controller:'0', bus:'0', unit:'1'",
                new HashMap<>(),
                true, false, false, "alias", Collections.singletonMap("prop1", "value1"), null, null);
    }

    @Test
    public void testGetVmDeviceByVmIdFilteringSetToFalse() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmId(EXISTING_VM_ID);
        assertGetVMDeviceByIdResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdFilteringWithPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmId(EXISTING_VM_ID,
                        PRIVILEGED_USER_ID,
                        true);
        assertGetVMDeviceByIdResult(devices);
    }

    @Test
    public void testGetVmDeviceByVmIdFilteringWithoutPermissions() {
        List<VmDevice> devices =
                dao.getVmDeviceByVmId(EXISTING_VM_ID,
                        UNPRIVILEGED_USER_ID,
                        true);
        assertTrue(devices.isEmpty(), "A user without any permissions should not see any devices");
    }
}
