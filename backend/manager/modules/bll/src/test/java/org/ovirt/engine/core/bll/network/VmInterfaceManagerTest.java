package org.ovirt.engine.core.bll.network;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.di.InjectorRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class VmInterfaceManagerTest {

    private static final String NETWORK_NAME = "networkName";
    private static final String VM_NAME = "vmName";
    private static final int OS_ID = 0;

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private MacPool macPool;

    @Mock
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;

    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private ExternalNetworkManager externalNetworkManager;

    private VmInterfaceManager vmInterfaceManager;

    @Before
    @SuppressWarnings("unchecked")
    public void setupMocks() {
        vmInterfaceManager = spy(new VmInterfaceManager(macPool));
        doReturn(vmNetworkStatisticsDao).when(vmInterfaceManager).getVmNetworkStatisticsDao();
        doReturn(vmNetworkInterfaceDao).when(vmInterfaceManager).getVmNetworkInterfaceDao();
        doReturn(vmNicDao).when(vmInterfaceManager).getVmNicDao();
        doReturn(vmDao).when(vmInterfaceManager).getVmDao();
        doNothing().when(vmInterfaceManager).auditLogMacInUseUnplug(any(VmNic.class));
        doNothing().when(vmInterfaceManager).removeFromExternalNetworks(anyList());

        doNothing().when(vmInterfaceManager).log(any(AuditLogableBase.class), any(AuditLogType.class));
    }

    @Test
    public void add() {
        runAddAndVerify(createNewInterface(), true);
    }

    @Test
    public void addWithExistingMacAddressSucceed() {
        runAddAndVerify(createNewInterface(), false);
    }

    protected void runAddAndVerify(VmNic iface, boolean reassignMac) {
        if (!reassignMac) {
            //we need to mock successful addition of MAC to pool, otherwise error is thrown
            when(macPool.addMac(eq(iface.getMacAddress()))).thenReturn(true);
        }

        vmInterfaceManager.add(iface, NoOpCompensationContext.getInstance(), reassignMac);
        if (reassignMac) {
            verify(macPool).allocateNewMac();
        } else {
            verify(macPool).addMac(iface.getMacAddress());
        }

        verify(vmNicDao).save(iface);
        verify(vmNetworkStatisticsDao).save(iface.getStatistics());
    }



    @Test
    public void removeAll() {
        List<VmNic> interfaces = Arrays.asList(createNewInterface(), createNewInterface());

        when(vmNicDao.getAllForVm(any(Guid.class))).thenReturn(interfaces);

        vmInterfaceManager.removeAllAndReleaseMacAddresses(Guid.newGuid());

        for (VmNic iface : interfaces) {
            verify(vmNicDao).remove(iface.getId());
            verify(vmNetworkStatisticsDao).remove(iface.getId());
        }

        verify(macPool).freeMacs(interfaces.stream().map(VmNic::getMacAddress).collect(Collectors.toList()));
    }

    /**
     * @return A new interface that can be used in tests.
     */
    private static VmNic createNewInterface() {
        VmNic iface = new VmNic();
        iface.setId(Guid.newGuid());
        iface.setMacAddress(RandomUtils.instance().nextString(10));

        return iface;
    }
}
