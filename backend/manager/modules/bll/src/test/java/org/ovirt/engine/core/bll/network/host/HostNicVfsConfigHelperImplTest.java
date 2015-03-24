package org.ovirt.engine.core.bll.network.host;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class HostNicVfsConfigHelperImplTest {

    private static final String NIC_NAME = RandomUtils.instance().nextString(5);
    private static final Guid NIC_ID = Guid.newGuid();
    private static final Guid HOST_ID = Guid.newGuid();
    private static final String NET_DEVICE_NAME = RandomUtils.instance().nextString(5);
    private static final String PCI_DEVICE_NAME = RandomUtils.instance().nextString(5);
    private static int TOTAL_NUM_OF_VFS = 7;

    @Mock
    private HostDevice netDevice;

    @Mock
    private HostDevice pciDevice;

    @Mock
    private VdsNetworkInterface nic;

    @Mock
    private HostNicVfsConfig hostNicVfsConfig;

    @Mock
    private InterfaceDao interfaceDao;

    @Mock
    private HostDeviceDao hostDeviceDao;

    @Mock
    private HostNicVfsConfigDao hostNicVfsConfigDao;

    @Captor
    private ArgumentCaptor<Collection<HostDevice>> hostDevicesCaptor;

    private HostNicVfsConfigHelperImpl hostNicVfsConfigHelper;

    @Before
    public void setUp() {
        hostNicVfsConfigHelper = new HostNicVfsConfigHelperImpl(interfaceDao, hostDeviceDao, hostNicVfsConfigDao);

        when(netDevice.getHostId()).thenReturn(HOST_ID);
        when(netDevice.getDeviceName()).thenReturn(NET_DEVICE_NAME);
        when(netDevice.getNetworkInterfaceName()).thenReturn(NIC_NAME);
        when(netDevice.getParentDeviceName()).thenReturn(PCI_DEVICE_NAME);

        when(pciDevice.getHostId()).thenReturn(HOST_ID);
        when(pciDevice.getDeviceName()).thenReturn(PCI_DEVICE_NAME);
        when(hostDeviceDao.getHostDeviceByHostIdAndDeviceName(HOST_ID, PCI_DEVICE_NAME)).thenReturn(pciDevice);

        List<HostDevice> devices = new ArrayList<>();
        devices.add(netDevice);
        devices.add(pciDevice);
        mockHostDevices(devices);

        when(nic.getId()).thenReturn(NIC_ID);
        when(nic.getName()).thenReturn(NIC_NAME);
        when(nic.getVdsId()).thenReturn(HOST_ID);
        when(interfaceDao.get(NIC_ID)).thenReturn(nic);
        when(nic.getName()).thenReturn(NIC_NAME);

        when(hostNicVfsConfig.getNicId()).thenReturn(NIC_ID);
        when(hostNicVfsConfigDao.getByNicId(NIC_ID)).thenReturn(hostNicVfsConfig);
    }

    @Test
    public void getNicByPciDeviceNotParentOfNetDevice() {
        assertNull(hostNicVfsConfigHelper.getNicByPciDevice(netDevice));
    }

    @Test
    public void getNicByNetDeviceNoNic() {
        VdsNetworkInterface newNic = new VdsNetworkInterface();
        newNic.setName(netDevice.getNetworkInterfaceName() + "not");
        mockNics(Collections.singletonList(newNic), false);

        assertNull(hostNicVfsConfigHelper.getNicByPciDevice(pciDevice));
    }

    @Test
    public void getNicByNetDeviceValid() {
        mockNics(Collections.<VdsNetworkInterface> emptyList(), true);
        assertEquals(nic, hostNicVfsConfigHelper.getNicByPciDevice(pciDevice));
    }

    @Test
    public void isSriovNetworkDeviceNotSriov() {
        commonIsSriovDevice(false);
    }

    @Test
    public void isSriovNetworkDeviceSriov() {
        commonIsSriovDevice(true);
    }

    private void commonIsSriovDevice(boolean isSriov) {
        when(pciDevice.getTotalVirtualFunctions()).thenReturn(isSriov ? TOTAL_NUM_OF_VFS : null);

        assertEquals(isSriov, hostNicVfsConfigHelper.isSriovDevice(pciDevice));
    }

    @Test
    public void isNetworkDevicePossitive() {
        assertFalse(hostNicVfsConfigHelper.isNetworkDevice(pciDevice));
    }

    @Test
    public void isNetworkDeviceNegtive() {
        assertTrue(hostNicVfsConfigHelper.isNetworkDevice(netDevice));
    }

    @Test
    public void updateHostNicVfsConfigWithNumVfsData() {
        commonUpdateHostNicVfsConfigWithNumVfsData(4);
    }

    @Test
    public void updateHostNicVfsConfigWithNumVfsDataZeroVfs() {
        commonUpdateHostNicVfsConfigWithNumVfsData(0);
    }

    private void commonUpdateHostNicVfsConfigWithNumVfsData(int numOfVfs) {
        when(pciDevice.getTotalVirtualFunctions()).thenReturn(TOTAL_NUM_OF_VFS);
        List<HostDevice> vfs = mockVfsOnNetDevice(numOfVfs);
        mockHostDevices(vfs);

        hostNicVfsConfigHelper.updateHostNicVfsConfigWithNumVfsData(hostNicVfsConfig);

        verify(hostNicVfsConfig).setMaxNumOfVfs(TOTAL_NUM_OF_VFS);
        verify(hostNicVfsConfig).setNumOfVfs(numOfVfs);
    }

    @Test
    public void getHostNicVfsConfigsWithNumVfsDataByHostId() {
        when(hostNicVfsConfigDao.getAllVfsConfigByHostId(HOST_ID)).thenReturn(Collections.singletonList(hostNicVfsConfig));

        when(pciDevice.getTotalVirtualFunctions()).thenReturn(TOTAL_NUM_OF_VFS);
        List<HostDevice> vfs = mockVfsOnNetDevice(2);
        mockHostDevices(vfs);

        List<HostNicVfsConfig> vfsConfigList =
                hostNicVfsConfigHelper.getHostNicVfsConfigsWithNumVfsDataByHostId(HOST_ID);

        assertEquals(1, vfsConfigList.size());
        assertEquals(hostNicVfsConfig, vfsConfigList.get(0));

        verify(hostNicVfsConfig).setMaxNumOfVfs(TOTAL_NUM_OF_VFS);
        verify(hostNicVfsConfig).setNumOfVfs(2);
    }

    private List<HostDevice> mockVfsOnNetDevice(int numOfVfs) {
        List<HostDevice> vfs = new ArrayList<>();

        for (int i = 0; i < numOfVfs; ++i) {
            HostDevice vfPciDevice = new HostDevice();
            vfPciDevice.setParentPhysicalFunction(pciDevice.getDeviceName());
            vfPciDevice.setDeviceName(String.valueOf(i));
            vfPciDevice.setHostId(HOST_ID);
            vfs.add(vfPciDevice);
        }

        return vfs;
    }

    private void mockHostDevices(List<HostDevice> extraDevices) {
        List<HostDevice> devices = new ArrayList<>();
        devices.add(pciDevice);
        devices.add(netDevice);
        devices.addAll(extraDevices);

        when(hostDeviceDao.getHostDevicesByHostId(HOST_ID)).thenReturn(devices);
    }

    @Test
    public void areAllVfsFreeNotSriovNic() {
        commonIsSriovDevice(false);
        try {
            hostNicVfsConfigHelper.areAllVfsFree(nic);
        } catch (Exception exception) {
            assertTrue(exception instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void areAllVfsFreeTrueNoVfs() {
        freeVfCommon(0, 0, 0, 0, 0);
        assertTrue(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeFalseAttachedToVm() {
        freeVfCommon(7, 3, 0, 0, 0);
        assertFalse(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeFalseNoNic() {
        freeVfCommon(6, 0, 1, 0, 0);
        assertFalse(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeFalseHasNetwork() {
        freeVfCommon(2, 0, 0, 3, 0);
        assertFalse(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeFalseHasVlanDevice() {
        freeVfCommon(4, 0, 0, 0, 3);
        assertFalse(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeTrue() {
        freeVfCommon(5, 0, 0, 0, 0);
        assertTrue(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    @Test
    public void areAllVfsFreeFalseMix() {
        freeVfCommon(1, 2, 3, 4, 5);
        assertFalse(hostNicVfsConfigHelper.areAllVfsFree(nic));
    }

    private List<HostDevice> freeVfCommon(int numOfFreeVfs,
            int numOfVfsAttachedToVm,
            int numOfVfsHasNoNic,
            int numOfVfsHasNetworkAttached,
            int numOfVfsHasVlanDeviceAttached) {
        hostNicVfsConfigHelper = spy(new HostNicVfsConfigHelperImpl(interfaceDao, hostDeviceDao, hostNicVfsConfigDao));

        List<HostDevice> devices = new ArrayList<>();
        List<HostDevice> freeVfs = new ArrayList<>();

        int numOfVfs =
                numOfFreeVfs + numOfVfsAttachedToVm + numOfVfsHasNoNic + numOfVfsHasNetworkAttached
                        + numOfVfsHasVlanDeviceAttached;
        List<HostDevice> vfs = mockVfsOnNetDevice(numOfVfs);
        List<VdsNetworkInterface> nics = new ArrayList<>();
        devices.addAll(vfs);

        for (HostDevice vfPciDevice : vfs) {
            HostDevice vfNetDevice = mockNetworkDeviceForPciDevice(vfPciDevice);
            devices.add(vfNetDevice);

            if (numOfVfsHasNoNic != 0) {
                --numOfVfsHasNoNic;
            } else {
                VdsNetworkInterface vfNic = mockNicForNetDevice(vfNetDevice);
                nics.add(vfNic);
                if (numOfVfsAttachedToVm != 0) {
                    --numOfVfsAttachedToVm;
                    vfPciDevice.setVmId(Guid.newGuid());
                } else if (numOfVfsHasNetworkAttached != 0) {
                    --numOfVfsHasNetworkAttached;
                    vfNic.setNetworkName("netName");
                } else if (numOfVfsHasVlanDeviceAttached != 0) {
                    --numOfVfsHasVlanDeviceAttached;
                    doReturn(true).when(hostNicVfsConfigHelper)
                            .isVlanDeviceAttached(vfNic);
                } else {
                    doReturn(false).when(hostNicVfsConfigHelper)
                            .isVlanDeviceAttached(vfNic);
                    freeVfs.add(vfPciDevice);
                }
            }
        }

        mockHostDevices(devices);
        mockNics(nics, true);

        return freeVfs;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getFreeVfNotSriovNic() {
        commonIsSriovDevice(false);
        hostNicVfsConfigHelper.getFreeVf(nic);
    }

    @Test
    public void getFreeVfNoVfs() {
        freeVfCommon(0, 0, 0, 0, 0);
        assertNull(hostNicVfsConfigHelper.getFreeVf(nic));
    }

    @Test
    public void getFreeVfNoFreeVf() {
        freeVfCommon(0, 1, 2, 3, 4);
        assertNull(hostNicVfsConfigHelper.getFreeVf(nic));
    }

    @Test
    public void getFreeVfOneFreeVf() {
        List<HostDevice> freeVfs = freeVfCommon(1, 4, 3, 2, 1);
        assertEquals(1, freeVfs.size());
        assertTrue(freeVfs.contains(hostNicVfsConfigHelper.getFreeVf(nic)));
    }

    @Test
    public void getFreeVfMoreThanOneFreeVf() {
        List<HostDevice> freeVfs = freeVfCommon(5, 2, 2, 2, 2);
        assertEquals(5, freeVfs.size());
        assertTrue(freeVfs.contains(hostNicVfsConfigHelper.getFreeVf(nic)));
    }

    private VdsNetworkInterface mockNicForNetDevice(HostDevice netDeviceParam) {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setVdsId(netDeviceParam.getHostId());
        nic.setName(netDeviceParam.getNetworkInterfaceName());

        return nic;
    }

    private void mockNics(List<VdsNetworkInterface> extraNics, boolean includeDefault) {
        List<VdsNetworkInterface> nics = new ArrayList<>();

        if (includeDefault) {
            nics.add(nic);
        }

        nics.addAll(extraNics);

        when(interfaceDao.getAllInterfacesForVds(HOST_ID)).thenReturn(nics);
    }

    private HostDevice mockNetworkDeviceForPciDevice(HostDevice pciDeviceParam) {
        HostDevice mockedNetDevice = new HostDevice();
        mockedNetDevice.setParentDeviceName(pciDeviceParam.getDeviceName());
        mockedNetDevice.setHostId(pciDeviceParam.getHostId());
        mockedNetDevice.setDeviceName(pciDeviceParam.getDeviceName() + "netDevice");
        mockedNetDevice.setNetworkInterfaceName(mockedNetDevice.getDeviceName() + "iface");

        return mockedNetDevice;
    }

    @Test
    public void getPciDeviceNameByNic() {
        assertEquals(PCI_DEVICE_NAME, hostNicVfsConfigHelper.getPciDeviceNameByNic(nic));
    }

    @Test
    public void setVmIdOnVfs() {
        List<HostDevice> vfs = mockVfsOnNetDevice(1);
        mockHostDevices(vfs);

        HostDevice vf = vfs.get(0);
        Guid vmId = Guid.newGuid();
        vf.setVmId(vmId);
        hostNicVfsConfigHelper.setVmIdOnVfs(HOST_ID, vmId, Collections.singleton(vf.getDeviceName()));

        verify(hostDeviceDao).updateAllInBatch(hostDevicesCaptor.capture());

        Collection<HostDevice> capturedDevices = hostDevicesCaptor.getValue();

        assertEquals(1, capturedDevices.size());
        assertThat(capturedDevices, hasItem(vf));
    }
}
