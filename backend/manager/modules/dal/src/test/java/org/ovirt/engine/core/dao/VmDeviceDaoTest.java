package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Unit tests to validate {@link VmDeviceDao}.
 */
public class VmDeviceDaoTest extends BaseGenericDaoTestCase<VmDeviceId, VmDevice, VmDeviceDao> {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_VM_ID_2 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid EXISTING_VM_ID_3 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5002");
    private static final Guid EXISTING_DEVICE_ID = new Guid("e14ed6f0-3b12-11e1-b614-63d00126418d");
    private static final Guid NON_EXISTING_VM_ID = Guid.newGuid();
    private static final int TOTAL_DEVICES = 15;
    private static final int TOTAL_DISK_DEVICES_FOR_EXISTING_VM = 5;
    private static final int TOTAL_HOST_DEVICES = 3;
    private static final int TOTAL_DEVICES_FOR_EXISTING_VM = 8;

    @Override
    protected VmDeviceId generateNonExistingId() {
        return new VmDeviceId(Guid.newGuid(), Guid.newGuid());
    }

    @Override
    protected int getEneitiesTotalCount() {
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
    protected VmDeviceDao prepareDao() {
        return dbFacade.getVmDeviceDao();
    }

    @Override
    protected VmDeviceId getExistingEntityId() {
        return new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID);
    }

    @Test
    public void existsForExistingVmDevice() throws Exception {
        assertTrue(dao.exists(new VmDeviceId(EXISTING_DEVICE_ID, EXISTING_VM_ID)));
    }

    @Test
    public void existsForNonExistingVmDevice() throws Exception {
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
        assertTrue("A user without any permissions should not see any devices", devices.isEmpty());
    }

    /**
     * Asserts all the disk devices are present in a result of {@link VmDeviceDao#getVmDeviceByVmIdTypeAndDevice(Guid, String, String)}
     * @param devices The result to check
     */
    private static void assertGetVMDeviceByIdTypeAndDeviceFullResult(List<VmDevice> devices) {
        assertEquals("there should only be " + TOTAL_DISK_DEVICES_FOR_EXISTING_VM + " disks",
                TOTAL_DISK_DEVICES_FOR_EXISTING_VM, devices.size());
    }

    /**
     * Asserts all the devices are present in a result of {@link VmDeviceDao#getVmDeviceByVmId(Guid, String, String)}
     * @param devices The result to check
     */
    private static void assertGetVMDeviceByIdResult(List<VmDevice> devices) {
        assertEquals("there should only be " + TOTAL_DEVICES_FOR_EXISTING_VM + " devices",
                TOTAL_DEVICES_FOR_EXISTING_VM, devices.size());
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
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));
        vmDevice.setIsPlugged(newPluggedValue);
        dao.updateHotPlugDisk(vmDevice);
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.getIsPlugged(), newPluggedValue);
    }

    @Test
    public void testUpdateBootOrder() {
        VmDevice vmDevice = dao.get(getExistingEntityId());
        int newBootOrderValue = vmDevice.getBootOrder() + 1;
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));
        vmDevice.setBootOrder(newBootOrderValue);
        dao.updateBootOrder(vmDevice);
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.getBootOrder(), newBootOrderValue);
    }

    @Test
    public void testUpdateBootOrderInBatch() {
        VmDevice vmDevice = dao.get(getExistingEntityId());
        int newBootOrderValue = vmDevice.getBootOrder() + 1;
        Assert.assertTrue(StringUtils.isNotBlank(vmDevice.getAddress()));
        vmDevice.setBootOrder(newBootOrderValue);
        dao.updateBootOrderInBatch(Arrays.asList(vmDevice));
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.getBootOrder(), newBootOrderValue);
    }

    @Test
    public void testExistsVmDeviceByVmIdAndType() {
        assertTrue(dao.existsVmDeviceByVmIdAndType(EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV));
        assertFalse(dao.existsVmDeviceByVmIdAndType(NON_EXISTING_VM_ID, VmDeviceGeneralType.HOSTDEV));
    }

    @Test
    public void testGetVmDeviceByType() {
        List<VmDevice> devices = dao.getVmDeviceByType(VmDeviceGeneralType.HOSTDEV);
        assertEquals("Expected to retrieve " + TOTAL_HOST_DEVICES + " host devices.", TOTAL_HOST_DEVICES, devices.size());
        Set<Guid> vmIds = new HashSet<>();
        for (VmDevice device : devices) {
            vmIds.add(device.getVmId());
        }
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

    public void testUpdateVmDeviceUsingScsiReservationProperty() {
        VmDevice vmDevice = dao.get(getExistingEntityId());
        boolean usingScsiReservation = !vmDevice.isUsingScsiReservation();
        vmDevice.setUsingScsiReservation(usingScsiReservation);
        dao.update(vmDevice);
        dao.get(getExistingEntityId());
        assertEquals(vmDevice.isUsingScsiReservation(), usingScsiReservation);
    }

    private VmDevice createVmDevice(Guid vmGuid) {
        return new VmDevice(new VmDeviceId(Guid.newGuid(), vmGuid),
                VmDeviceGeneralType.DISK,
                "floppy",
                "type:'drive', controller:'0', bus:'0', unit:'1'",
                2,
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
        assertTrue("A user without any permissions should not see any devices", devices.isEmpty());
    }
}
