package org.ovirt.engine.core.bll.network.cluster.helper;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class DisplayNetworkClusterHelperTest {

    private static final String TEST_NETWORK_NAME = "test network";
    private static final Guid TEST_NETWORK_ID = new Guid("1-2-3-4-5");
    private static final Guid TEST_CLUSTER_ID = new Guid("a-b-c-d-e");
    private static final NetworkClusterId TEST_NETWORK_CLUSTER_ID = new NetworkClusterId(
            TEST_CLUSTER_ID,
            TEST_NETWORK_ID);

    @Mock
    private NetworkClusterDao mockNetworkClusterDao;
    @Mock
    private VmDao mockVmDao;
    @Mock
    private NetworkCluster mockNetworkCluster;
    @Mock
    private NetworkCluster mockNetworkClusterBeforeUpdate;
    @Mock
    private VM mockVm;
    @Mock
    private AuditLogDirector mockAuditLogDirector;

    @Captor
    private ArgumentCaptor<AuditLogableBase> auditLogableBaseCaptor;

    private DisplayNetworkClusterHelper underTest;

    @Before
    public void setUp() {
        underTest = new DisplayNetworkClusterHelper(
                mockNetworkClusterDao,
                mockVmDao,
                mockNetworkCluster,
                TEST_NETWORK_NAME,
                mockAuditLogDirector);

        Mockito.when(mockNetworkCluster.getId()).thenReturn(TEST_NETWORK_CLUSTER_ID);
        Mockito.when(mockNetworkClusterDao.get(TEST_NETWORK_CLUSTER_ID)).thenReturn(mockNetworkClusterBeforeUpdate);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#isDisplayToBeUpdated()}.
     */
    @Test
    public void testIsDisplayToBeUpdatedPositive1() {

        testIsDisplayToBeUpdatedInner(true, false, true);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#isDisplayToBeUpdated()}.
     */
    @Test
    public void testIsDisplayToBeUpdatedPositive2() {

        testIsDisplayToBeUpdatedInner(false, true, true);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#isDisplayToBeUpdated()}.
     */
    @Test
    public void testIsDisplayToBeUpdatedNegative1() {

        testIsDisplayToBeUpdatedInner(true, true, false);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#isDisplayToBeUpdated()}.
     */
    @Test
    public void testIsDisplayToBeUpdatedNegative2() {

        testIsDisplayToBeUpdatedInner(false, false, false);
    }

    private void testIsDisplayToBeUpdatedInner(boolean displayNetworkToBeSet,
            boolean displayNetworkBeforeUpdate,
            boolean expectedResult) {
        Mockito.when(mockNetworkCluster.isDisplay()).thenReturn(displayNetworkToBeSet);
        Mockito.when(mockNetworkClusterBeforeUpdate.isDisplay()).thenReturn(displayNetworkBeforeUpdate);

        final boolean actual = underTest.isDisplayToBeUpdated();

        Mockito.verify(mockNetworkCluster).getId();
        Mockito.verify(mockNetworkClusterDao).get(TEST_NETWORK_CLUSTER_ID);
        Mockito.verify(mockNetworkCluster).isDisplay();
        Mockito.verify(mockNetworkClusterBeforeUpdate).isDisplay();

        Assert.assertEquals(expectedResult, actual);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#warnOnActiveVm()}.
     */
    @Test
    public void testWarnOnActiveVmPositive() {

        testWarnOnActiveVmInner(true);

        Mockito.verify(mockAuditLogDirector).log(auditLogableBaseCaptor.capture(),
                Mockito.same(AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM));

        final AuditLogableBase actualLoggable = auditLogableBaseCaptor.getValue();
        Assert.assertEquals(TEST_CLUSTER_ID, actualLoggable.getClusterId());
        Assert.assertEquals(TEST_NETWORK_NAME, actualLoggable.getCustomValue("networkname"));
    }

    @Test
    public void testWarnOnActiveVmNegative() {

        testWarnOnActiveVmInner(false);

        Mockito.verifyZeroInteractions(mockAuditLogDirector);
    }

    private void testWarnOnActiveVmInner(boolean activeVm) {
        final List<VM> clusterVms = Collections.singletonList(mockVm);

        Mockito.when(mockNetworkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
        Mockito.when(mockVmDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(clusterVms);
        Mockito.when(mockVm.isRunning()).thenReturn(activeVm);

        underTest.warnOnActiveVm();

        Mockito.verify(mockNetworkCluster, Mockito.atLeastOnce()).getClusterId();
        Mockito.verify(mockVmDao).getAllForCluster(TEST_CLUSTER_ID);
        Mockito.verify(mockVm).isRunning();
    }
}
