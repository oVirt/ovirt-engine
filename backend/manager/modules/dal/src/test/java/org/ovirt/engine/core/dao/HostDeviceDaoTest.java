package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.compat.Guid;

public class HostDeviceDaoTest extends BaseGenericDaoTestCase<HostDeviceId, HostDevice, HostDeviceDao> {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_VM_ID_2 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid EXISTING_VM_ID_3 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5002");

    private static final Guid EXISTING_HOST_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");

    private static final String EXISTING_VM_NAME = "rhel5-pool-57";
    private static final String EXISTING_VM_NAME_2 = "1";
    private static final String EXISTING_VM_NAME_3 = "rhel5-pool-52";

    private static final String EXISTING_DEVICE_NAME = "pci_0000_00_1f_0";
    private static final String EXISTING_DEVICE_NAME_2 = "pci_0000_00_1f_2";

    private static final int EXISTING_IOMMU_GROUP = 9;
    private static final int TOTAL_DEVICES_IN_GROUP = 4;

    @Override
    protected HostDevice generateNewEntity() {
        HostDevice device = new HostDevice();
        device.setHostId(EXISTING_HOST_ID);
        device.setDeviceName(EXISTING_DEVICE_NAME + "___child");
        device.setParentDeviceName(EXISTING_DEVICE_NAME);
        device.setCapability("pci");
        device.setDriver("mock driver");
        device.setAssignable(true);

        return device;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setIommuGroup(null);
        existingEntity.setProductName("device upgrade");
    }

    @Override
    protected HostDeviceId getExistingEntityId() {
        return new HostDeviceId(EXISTING_HOST_ID, EXISTING_DEVICE_NAME);
    }

    @Override
    protected HostDeviceDao prepareDao() {
        return dbFacade.getHostDeviceDao();
    }

    @Override
    protected HostDeviceId generateNonExistingId() {
        return new HostDeviceId(Guid.newGuid(), "this_device_probably_doesnt_exist");
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 36;
    }

    @Test
    public void saveNetworkDevice() {
        HostDevice netDevice = generateNewEntity();

        netDevice.setCapability("net");
        netDevice.setNetworkInterfaceName("eth1");

        dao.save(netDevice);

        HostDevice result = dao.get(netDevice.getId());

        assertNotNull(result);
        assertEquals(netDevice, result);
    }

    @Test
    public void updateNetworkDevice() {
        HostDevice before = getNetworkDevice();

        before.setNetworkInterfaceName(before.getNetworkInterfaceName() + "new");
        before.setDriver("updated driver");

        dao.update(before);

        HostDevice after = dao.get(before.getId());

        assertNotNull(after);
        assertEquals(before, after);
    }

    @Test
    public void testGetHostDevicesByHostIdAndIommuGroup() {
        List<HostDevice> hostDevices = dao.getHostDevicesByHostIdAndIommuGroup(EXISTING_HOST_ID, EXISTING_IOMMU_GROUP);
        assertEquals(TOTAL_DEVICES_IN_GROUP, hostDevices.size());
        for (HostDevice hostDevice : hostDevices) {
            assertEquals(EXISTING_HOST_ID, hostDevice.getHostId());
            assertEquals(Integer.valueOf(EXISTING_IOMMU_GROUP), hostDevice.getIommuGroup());
        }
    }

    @Test
    public void testGetVmExtendedHostDevicesByVmId() {
        List<HostDeviceView> hostDevices = dao.getVmExtendedHostDevicesByVmId(EXISTING_VM_ID);
        assertEquals(1, hostDevices.size());
        assertEquals(EXISTING_VM_ID, hostDevices.get(0).getConfiguredVmId());
        assertSetEquals(Collections.singletonList(EXISTING_VM_NAME), hostDevices.get(0).getAttachedVmNames());
        assertNull(hostDevices.get(0).getRunningVmName());
        assertNull(hostDevices.get(0).getRunningVmId());

        hostDevices = dao.getVmExtendedHostDevicesByVmId(EXISTING_VM_ID_3);
        assertEquals(1, hostDevices.size());
        assertEquals(EXISTING_VM_ID_3, hostDevices.get(0).getConfiguredVmId());
        assertSetEquals(Arrays.asList(EXISTING_VM_NAME_2, EXISTING_VM_NAME_3), hostDevices.get(0).getAttachedVmNames());
        assertEquals(EXISTING_VM_NAME_2, hostDevices.get(0).getRunningVmName());
        assertEquals(EXISTING_VM_ID_2, hostDevices.get(0).getRunningVmId());
    }

    @Test
    public void testGetExtendedHostDevicesByHostId() {
        List<HostDeviceView> hostDevices = dao.getExtendedHostDevicesByHostId(EXISTING_HOST_ID);
        assertEquals(getEneitiesTotalCount(), hostDevices.size());
        for (HostDeviceView hostDevice : hostDevices) {
            assertEquals(EXISTING_HOST_ID, hostDevice.getHostId());
        }
    }

    @Test
    public void testCheckVmHostDeviceAvailability() {
        assertTrue(dao.checkVmHostDeviceAvailability(EXISTING_VM_ID, EXISTING_HOST_ID));
    }

    @Test
    public void testCheckVmHostDeviceAvailabilityOnAlreadyAllocatedDevice() {
        assertTrue(dao.checkVmHostDeviceAvailability(EXISTING_VM_ID_2, EXISTING_HOST_ID));
    }

    @Test
    public void testCheckVmHostDeviceAvailabilityOnAllocatedToDifferentVm() {
        assertFalse(dao.checkVmHostDeviceAvailability(EXISTING_VM_ID_3, EXISTING_HOST_ID));
    }

    @Test
    public void testMarkHostDevicesUsedByVmId() {
        dao.markHostDevicesUsedByVmId(EXISTING_VM_ID, EXISTING_HOST_ID);
        HostDevice hostDevice = dao.getHostDeviceByHostIdAndDeviceName(EXISTING_HOST_ID, EXISTING_DEVICE_NAME);
        assertEquals(EXISTING_VM_ID, hostDevice.getVmId());
    }

    @Test
    public void testFreeHostDevicesUsedByVmId() {
        dao.freeHostDevicesUsedByVmId(EXISTING_VM_ID_2);
        HostDevice hostDevice = dao.getHostDeviceByHostIdAndDeviceName(EXISTING_HOST_ID, EXISTING_DEVICE_NAME_2);
        assertNull(hostDevice.getVmId());
    }

    private <T> void assertSetEquals(Collection<T> expected, Collection<T> actual) {
        Set<T> expectedSet = new HashSet<>();
        expectedSet.addAll(expected);
        Set<T> actualSet = new HashSet<>();
        actualSet.addAll(actual);
        assertEquals(expectedSet, actualSet);
    }

    @Test
    public void setVmIdOnHostDeviceTest() {
        HostDevice before = getNetworkDevice();
        assertNull(before.getVmId());

        Guid vmId = FixturesTool.VM_WITH_NO_ATTACHED_DISKS;
        dao.setVmIdOnHostDevice(before.getId(), vmId);

        before.setVmId(vmId);
        HostDevice after = dao.get(before.getId());
        assertEquals(before, after);
    }

    @Test
    public void cleanDownVmsTest() {
        HostDevice device = getNetworkDevice();
        HostDeviceId deviceId = device.getId();

        // Setting an id of VM with <code>VMStatus.Down</code>
        Guid vmId = FixturesTool.VM_WITH_NO_ATTACHED_DISKS;
        dao.setVmIdOnHostDevice(deviceId, vmId);

        device = dao.get(deviceId);
        assertNotNull(device);
        assertEquals(vmId, device.getVmId());

        dao.cleanDownVms();

        device = dao.get(deviceId);
        assertNull(device.getVmId());
    }

    private HostDevice getNetworkDevice() {
        HostDeviceId netDeviceId =
                new HostDeviceId(FixturesTool.NETWORK_HOST_DEVICE_HOST_ID, FixturesTool.NETWORK_HOST_DEVICE_NAME);
        return dao.get(netDeviceId);
    }
}
