package org.ovirt.engine.core.bll.network;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class VmInterfaceManagerTest {

    private static final int OS_ID = 0;

    @Mock
    private MacPool macPool;

    @Mock
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;

    @Mock
    private VmNicDao vmNicDao;

    private VmInterfaceManager vmInterfaceManager;

    @Mock
    private Version version;

    @Before
    @SuppressWarnings("unchecked")
    public void setupMocks() {
        vmInterfaceManager = spy(new VmInterfaceManager(macPool));
        doReturn(vmNetworkStatisticsDao).when(vmInterfaceManager).getVmNetworkStatisticsDao();
        doReturn(vmNicDao).when(vmInterfaceManager).getVmNicDao();
        doNothing().when(vmInterfaceManager).removeFromExternalNetworks(anyList());
    }

    @Test
    public void add() {
        runAddAndVerify(createNewInterface(), false, times(0), OS_ID);
    }

    @Test
    public void addWithExistingMacAddressSucceed() {
        VmNic iface = createNewInterface();
        runAddAndVerify(iface, true, times(1), OS_ID);
    }

    protected void runAddAndVerify(VmNic iface,
            boolean reserveExistingMac,
            VerificationMode addMacVerification,
            int osId) {
        OsRepository osRepository = mock(OsRepository.class);
        when(vmInterfaceManager.getOsRepository()).thenReturn(osRepository);
        when(osRepository.hasNicHotplugSupport(any(Integer.class), any(Version.class))).thenReturn(true);
        vmInterfaceManager.add(iface, NoOpCompensationContext.getInstance(), reserveExistingMac, false, osId, version);
        if (reserveExistingMac) {
            verify(macPool, times(1)).forceAddMac(iface.getMacAddress());
        } else {
            verifyZeroInteractions(macPool);
        }
        verifyAddDelegatedCorrectly(iface, addMacVerification);
    }



    @Test
    public void removeAll() {
        List<VmNic> interfaces = Arrays.asList(createNewInterface(), createNewInterface());

        when(vmNicDao.getAllForVm(any(Guid.class))).thenReturn(interfaces);

        vmInterfaceManager.removeAll(Guid.newGuid());

        for (VmNic iface : interfaces) {
            verifyRemoveAllDelegatedCorrectly(iface);
        }
    }

    /**
     * Verify that {@link VmInterfaceManager#add} delegated correctly to {@link MacPool} & Daos.
     *
     * @param iface
     *            The interface to check for.
     * @param addMacVerification
     *            Mode to check (times(1), never(), etc) for {@link MacPool#addMac(String)}.
     */
    protected void verifyAddDelegatedCorrectly(VmNic iface, VerificationMode addMacVerification) {
        verify(macPool, addMacVerification).forceAddMac(iface.getMacAddress());
        verify(vmNicDao).save(iface);
        verify(vmNetworkStatisticsDao).save(iface.getStatistics());
    }

    /**
     * Verify that {@link VmInterfaceManager#removeAll} delegated correctly to {@link MacPool} & Daos.
     *
     * @param iface
     *            The interface to check for.
     */
    protected void verifyRemoveAllDelegatedCorrectly(VmNic iface) {
        verify(macPool, times(1)).freeMac(iface.getMacAddress());
        verify(vmNicDao).remove(iface.getId());
        verify(vmNetworkStatisticsDao).remove(iface.getId());
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
