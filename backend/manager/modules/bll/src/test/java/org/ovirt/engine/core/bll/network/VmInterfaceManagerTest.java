package org.ovirt.engine.core.bll.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.dao.VmNetworkStatisticsDAO;
import org.ovirt.engine.core.utils.RandomUtils;

public class VmInterfaceManagerTest {

    private final String NETWORK_NAME = "networkName";
    private final String VM_NAME = "vmName";

    @Mock
    private MacPoolManager macPoolManager;

    @Mock
    private VmNetworkStatisticsDAO vmNetworkStatisticsDAO;

    @Mock
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;

    @Mock
    private VmDAO vmDAO;

    @Spy
    private VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        doReturn(macPoolManager).when(vmInterfaceManager).getMacPoolManager();
        doReturn(vmNetworkStatisticsDAO).when(vmInterfaceManager).getVmNetworkStatisticsDAO();
        doReturn(vmNetworkInterfaceDAO).when(vmInterfaceManager).getVmNetworkInterfaceDAO();
        doReturn(vmDAO).when(vmInterfaceManager).getVmDAO();
        doNothing().when(vmInterfaceManager).auditLogMacInUse(any(VmNetworkInterface.class));

        doNothing().when(vmInterfaceManager).log(any(AuditLogableBase.class), any(AuditLogType.class));
    }

    @Test
    public void add() {
        runAddAndVerify(createNewInterface(), true, times(1));
    }

    @Test
    public void addWithExistingMacAddress() {
        VmNetworkInterface iface = createNewInterface();

        try {
            runAddAndVerify(iface, false, times(1));
            fail(String.format("Expected to catch %s, but didn't.", VdcBLLException.class.getSimpleName()));
        } catch (VdcBLLException e) {
            assertEquals(VdcBllErrors.MAC_ADDRESS_IS_IN_USE, e.getErrorCode());
        } catch (Exception e) {
            fail(String.format("Expected to catch %s, but caught %s instead.",
                    VdcBLLException.class.getSimpleName(),
                    e.getClass().getSimpleName()));
        }
        verify(vmInterfaceManager).auditLogMacInUse(iface);
    }

    protected void runAddAndVerify(VmNetworkInterface iface,
            boolean addMacResult,
            VerificationMode addMacVerification) {
        when(macPoolManager.addMac(iface.getMacAddress())).thenReturn(addMacResult);

        vmInterfaceManager.add(iface, NoOpCompensationContext.getInstance(), false);
        verifyAddDelegatedCorrectly(iface, addMacVerification);
    }

    @Test
    public void addAllocateNewMacAddress() {
        VmNetworkInterface iface = createNewInterface();
        String newMac = RandomUtils.instance().nextString(10);
        when(macPoolManager.allocateNewMac()).thenReturn(newMac);
        vmInterfaceManager.add(iface, NoOpCompensationContext.getInstance(), true);
        assertEquals(newMac, iface.getMacAddress());
    }

    @Test
    public void isValidVmNetworkForNullNetwork() {
        Network network = createNewNetwork(true, NETWORK_NAME);
        VmNetworkInterface iface = createNewInterface();
        assertTrue(vmInterfaceManager.isValidVmNetwork(iface, Collections.singletonMap(network.getName(), network)));
    }

    @Test
    public void isValidVmNetworkForValidNetwork() {
        Network network = createNewNetwork(true, NETWORK_NAME);
        VmNetworkInterface iface = createNewInterface();
        iface.setNetworkName(network.getName());
        assertTrue(vmInterfaceManager.isValidVmNetwork(iface, Collections.singletonMap(network.getName(), network)));
    }

    @Test
    public void isValidVmNetworkForNonVmNetwork() {
        Network network = createNewNetwork(false, NETWORK_NAME);
        VmNetworkInterface iface = createNewInterface();
        iface.setNetworkName(network.getName());
        assertFalse(vmInterfaceManager.isValidVmNetwork(iface, Collections.singletonMap(network.getName(), network)));
    }

    @Test
    public void isValidVmNetworkForNetworkNotInVds() {
        VmNetworkInterface iface = createNewInterface();
        iface.setNetworkName(NETWORK_NAME);
        assertFalse(vmInterfaceManager.isValidVmNetwork(iface, Collections.<String, Network> emptyMap()));
    }

    @Test
    public void findActiveVmsUsingNetworks() {
        mockDaos();

        List<String> vmNames =
                vmInterfaceManager.findActiveVmsUsingNetworks(Guid.NewGuid(), Collections.singletonList(NETWORK_NAME));
        assertTrue(vmNames.contains(VM_NAME));
    }

    @Test
    public void findNoneOfActiveVmsUsingNetworks() {
        mockDaos();

        List<String> vmNames =
                vmInterfaceManager.findActiveVmsUsingNetworks(Guid.NewGuid(),
                        Collections.singletonList(NETWORK_NAME + "1"));
        assertTrue(vmNames.isEmpty());
    }

    @Test
    public void removeAll() {
        List<VmNetworkInterface> interfaces = Arrays.asList(createNewInterface(), createNewInterface());

        when(vmNetworkInterfaceDAO.getAllForVm(any(Guid.class))).thenReturn(interfaces);

        vmInterfaceManager.removeAll(Guid.NewGuid());

        for (VmNetworkInterface iface : interfaces) {
            verifyRemoveAllDelegatedCorrectly(iface);
        }
    }

    private void mockDaos() {
        VM vm = createVM(VM_NAME, NETWORK_NAME);
        when(vmDAO.getAllRunningForVds(any(Guid.class))).thenReturn(Arrays.asList(vm));
        when(vmNetworkInterfaceDAO.getAllForVm(vm.getId())).thenReturn(vm.getInterfaces());
    }

    /**
     * Verify that {@link VmInterfaceManager#add} delegated correctly to {@link MacPoolManager} & DAOs.
     *
     * @param iface
     *            The interface to check for.
     * @param addMacVerification
     *            Mode to check (times(1), never(), etc) for {@link MacPoolManager#addMac(String)}.
     */
    protected void verifyAddDelegatedCorrectly(VmNetworkInterface iface, VerificationMode addMacVerification) {
        verify(macPoolManager, addMacVerification).addMac(iface.getMacAddress());
        verify(vmNetworkInterfaceDAO).save(iface);
        verify(vmNetworkStatisticsDAO).save(iface.getStatistics());
    }

    /**
     * Verify that {@link VmInterfaceManager#removeAll} delegated correctly to {@link MacPoolManager} & DAOs.
     *
     * @param iface
     *            The interface to check for.
     */
    protected void verifyRemoveAllDelegatedCorrectly(VmNetworkInterface iface) {
        verify(macPoolManager, times(1)).freeMac(iface.getMacAddress());
        verify(vmNetworkInterfaceDAO).remove(iface.getId());
        verify(vmNetworkStatisticsDAO).remove(iface.getId());
    }

    /**
     * @return A new interface that can be used in tests.
     */
    protected VmNetworkInterface createNewInterface() {
        VmNetworkInterface iface = new VmNetworkInterface();
        iface.setId(Guid.NewGuid());
        iface.setMacAddress(RandomUtils.instance().nextString(10));
        return iface;
    }

    private Network createNewNetwork(boolean isVmNetwork, String networkName) {
        Network network = new Network();
        network.setVmNetwork(isVmNetwork);
        network.setname(networkName);
        return network;
    }

    /**
     * Creates a VM instance with a given name, having an interface which uses a given network.
     *
     * @param vmName
     *            The VM name to be set
     * @param networkName
     *            The network name to be set for the VM interface
     * @return the VM instance with the appropriate data.
     */
    private VM createVM(String vmName, String networkName) {
        VM vm = new VM();
        vm.setId(Guid.NewGuid());
        vm.setVmName(vmName);
        VmNetworkInterface vmIface = createNewInterface();
        vmIface.setVmId(vm.getId());
        vmIface.setNetworkName(networkName);
        vm.getInterfaces().add(vmIface);
        return vm;
    }
}
