package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class MultiQueueUtilsTest {

    @InjectMocks
    private MultiQueueUtils underTest;

    @Mock
    private VM vm;

    @Mock
    private VmStatic vmStatic;

    @Test
    public void isInterfaceQueuable() {
        // Plugged, interface + birdge, pv
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), Guid.newGuid()));
        assertTrue(underTest.isInterfaceQueuable(createVmDevice(/*bridge*/true), createVmNic(/*type*/VmInterfaceType.pv)));
        assertFalse(underTest.isInterfaceQueuable(createVmDevice(/*bridge*/false), createVmNic(/*type*/VmInterfaceType.pciPassthrough)));
        assertFalse(underTest.isInterfaceQueuable(createVmDevice(/*bridge*/true), createVmNic(/*type*/VmInterfaceType.e1000)));
    }

    private Guid nicId;

    private VmDevice createVmDevice(boolean bridge) {
        VmDevice vmDevice = new VmDevice();
        nicId = Guid.newGuid();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), nicId));
        vmDevice.setType(VmDeviceGeneralType.INTERFACE);
        vmDevice.setDevice(VmDeviceType.BRIDGE.getName());
        return vmDevice;
    }

    private VmNic createVmNic(VmInterfaceType type) {
        VmNic vmNic = new VmNic();
        vmNic.setId(nicId);
        vmNic.setType(type.getValue());
        return vmNic;
    }
}
