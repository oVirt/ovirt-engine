package org.ovirt.engine.core.bll.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
public class FindActiveVmsUsingNetworkTest {

    private static final String NETWORK_NAME = "networkName";
    private static final String VM_NAME = "vmName";
    private static final Version VERSION_3_2 = new Version(3, 2);
    private static final int OS_ID = 0;


    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private VmDao vmDao;

    @InjectMocks
    private FindActiveVmsUsingNetwork findActiveVmsUsingNetwork;

    @Test
    public void findActiveVmsUsingNetworks() {
        mockDaos(true);

        List<String> vmNames =
                findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(Guid.newGuid(), NETWORK_NAME);
        assertTrue(vmNames.contains(VM_NAME));
    }

    @Test
    public void findActiveVmsUsingNetworksOnUnpluggedVnic() {
        mockDaos(false);

        List<String> vmNames =
                findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(Guid.newGuid(), NETWORK_NAME);
        assertFalse(vmNames.contains(VM_NAME));
    }

    @Test
    public void findNoneOfActiveVmsUsingNetworks() {
        mockDaos(true);

        List<String> vmNames =
                findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(Guid.newGuid(), NETWORK_NAME + "1");
        assertTrue(vmNames.isEmpty());
    }

    private void mockDaos(boolean pluggedInterface) {
        VM vm = createVM(VM_NAME, NETWORK_NAME, pluggedInterface);
        when(vmDao.getAllRunningForVds(any())).thenReturn(Collections.singletonList(vm));
        when(vmNetworkInterfaceDao.getAllForVm(vm.getId())).thenReturn(vm.getInterfaces());
    }

    /**
     * Creates a VM instance with a given name, having an interface which uses a given network.
     *
     * @param vmName
     *            The VM name to be set
     * @param networkName
     *            The network name to be set for the VM interface
     * @param pluggedInterface
     *            whether the VM interface plugged or not
     * @return the VM instance with the appropriate data.
     */
    private static VM createVM(String vmName, String networkName, boolean pluggedInterface) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setName(vmName);
        VmNetworkInterface vmIface = createNewViewableInterface(pluggedInterface);
        vmIface.setVmId(vm.getId());
        vmIface.setNetworkName(networkName);
        vm.getInterfaces().add(vmIface);
        return vm;
    }

    private static VmNetworkInterface createNewViewableInterface(boolean plugged) {
        VmNetworkInterface iface = new VmNetworkInterface();
        iface.setId(Guid.newGuid());
        iface.setMacAddress(RandomUtils.instance().nextString(10));
        iface.setPlugged(plugged);
        return iface;
    }
}
