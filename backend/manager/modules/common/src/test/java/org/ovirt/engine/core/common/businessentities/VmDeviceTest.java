package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

public class VmDeviceTest {

    @Test
    public void testSelfAndIdentifyEquality() {
        Guid vmId = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2463");
        Guid deviceId = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2464");
        VmDevice device1 = createVmDevice(deviceId, vmId);
        assertEquals(0, device1.compareTo(device1));
        VmDevice device2 = createVmDevice(deviceId, vmId);
        assertEquals(0, device1.compareTo(device2));

    }

    @Test
    public void testCompareToVmIdsAreEqual() {
        // b23ad6d7-0df8-4d69-b4e5-d5b5e90c2463
        Guid vmId = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2463");
        Guid deviceId1 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2464");
        Guid deviceId2 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2465");
        VmDevice device1 = createVmDevice(deviceId1, vmId);
        VmDevice device2 = createVmDevice(deviceId2, vmId);
        assertCompareTo(device1, device2);

    }

    @Test
    public void testCompareToVmIdsAreNotEqual() {
        Guid vmId1 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2463");
        Guid vmId2 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2464");
        Guid deviceId1 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2465");
        Guid deviceId2 = new Guid("b23ad6d7-0df8-4d69-b4e5-d5b5e90c2466");
        VmDevice device1 = createVmDevice(deviceId1, vmId1);
        VmDevice device2 = createVmDevice(deviceId2, vmId2);
        assertCompareTo(device1, device2);
        // Test in case the device IDs are equal
        device1 = createVmDevice(deviceId1, vmId1);
        device2 = createVmDevice(deviceId1, vmId2);
        assertCompareTo(device1, device2);
    }

    private void assertCompareTo(VmDevice device1, VmDevice device2) {
        assertTrue(device2.compareTo(device1) > 0);
        assertTrue(device1.compareTo(device2) < 0);
    }

    private VmDevice createVmDevice(Guid deviceId, Guid vmId) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(deviceId, vmId));
        return device;

    }
}
