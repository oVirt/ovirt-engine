package org.ovirt.engine.core.bll.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.MacPoolManager;
import org.ovirt.engine.core.bll.context.NoOpCompensationContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.dao.VmNetworkStatisticsDAO;
import org.ovirt.engine.core.utils.RandomUtils;

public class VmInterfaceManagerTest {

    @Mock
    private MacPoolManager macPoolManager;

    @Mock
    private VmNetworkStatisticsDAO vmNetworkStatisticsDAO;

    @Mock
    private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;

    @Spy
    private VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        doReturn(macPoolManager).when(vmInterfaceManager).getMacPoolManager();
        doReturn(vmNetworkStatisticsDAO).when(vmInterfaceManager).getVmNetworkStatisticsDAO();
        doReturn(vmNetworkInterfaceDAO).when(vmInterfaceManager).getVmNetworkInterfaceDAO();

        doNothing().when(vmInterfaceManager).log(any(AuditLogableBase.class), any(AuditLogType.class));
    }

    @Test
    public void addReturnsTrueWithCallToMacPoolManager() {
        runAddAndVerify(createNewInterface(), true, times(1), true);
    }

    @Test
    public void addReturnsFalseWithCallToMacPoolManager() {
        runAddAndVerify(createNewInterface(), false, times(1), false);
    }

    @Test
    public void addLogsWhenMacAlreadyInUseAndReturnsFalse() {
        VmNetworkInterface iface = createNewInterface();
        when(macPoolManager.IsMacInUse(iface.getMacAddress())).thenReturn(true);

        runAddAndVerify(iface, true, never(), false);

        verify(vmInterfaceManager).log(any(AuditLogableBase.class), eq(AuditLogType.MAC_ADDRESS_IS_IN_USE));
    }

    protected void runAddAndVerify(VmNetworkInterface iface,
            boolean addMacResult,
            VerificationMode addMacVerification,
            boolean expectedResult) {
        when(macPoolManager.AddMac(iface.getMacAddress())).thenReturn(addMacResult);

        assertEquals(expectedResult, vmInterfaceManager.add(iface, NoOpCompensationContext.getInstance()));
        verifyAddDelegatedCorrectly(iface, addMacVerification);
    }

    @Test
    public void removeAllRemovesFromMacPoolAlso() {
        runRemoveAllAndVerify(true, times(1));
    }

    @Test
    public void removeAllDoesntRemoveFromMacPoolWhenNotNeeded() {
        runRemoveAllAndVerify(false, never());
    }

    protected void runRemoveAllAndVerify(boolean removeFromPool, VerificationMode freeMacVerification) {
        List<VmNetworkInterface> interfaces = Arrays.asList(createNewInterface(), createNewInterface());

        when(vmNetworkInterfaceDAO.getAllForVm(any(Guid.class))).thenReturn(interfaces);

        vmInterfaceManager.removeAll(removeFromPool, Guid.NewGuid());

        for (VmNetworkInterface iface : interfaces) {
            verifyRemoveAllDelegatedCorrectly(iface, freeMacVerification);
        }
    }

    /**
     * Verify that {@link VmInterfaceManager#add} delegated correctly to {@link MacPoolManager} & DAOs.
     *
     * @param iface
     *            The interface to check for.
     * @param addMacVerification
     *            Mode to check (times(1), never(), etc) for {@link MacPoolManager#AddMac(String)}.
     */
    protected void verifyAddDelegatedCorrectly(VmNetworkInterface iface, VerificationMode addMacVerification) {
        verify(macPoolManager, addMacVerification).AddMac(iface.getMacAddress());
        verify(vmNetworkInterfaceDAO).save(iface);
        verify(vmNetworkStatisticsDAO).save(iface.getStatistics());
    }

    /**
     * Verify that {@link VmInterfaceManager#removeAll} delegated correctly to {@link MacPoolManager} & DAOs.
     *
     * @param iface
     *            The interface to check for.
     * @param freeMacVerification
     *            Mode to check (times(1), never(), etc) for {@link MacPoolManager#freeMac(String)}.
     */
    protected void verifyRemoveAllDelegatedCorrectly(VmNetworkInterface iface, VerificationMode freeMacVerification) {
        verify(macPoolManager, freeMacVerification).freeMac(iface.getMacAddress());
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
}
