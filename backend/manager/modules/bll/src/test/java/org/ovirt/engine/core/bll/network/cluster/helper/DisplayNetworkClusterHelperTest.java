package org.ovirt.engine.core.bll.network.cluster.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DisplayNetworkClusterHelperTest {

    private static final String TEST_NETWORK_NAME = "test network";
    private static final String TEST_CLUSTER_NAME = "cluster name";
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
    private ClusterDao mockClusterDao;
    @Mock
    private NetworkCluster mockNetworkCluster;
    @Mock
    private NetworkCluster mockNetworkClusterBeforeUpdate;
    @Mock
    private VM mockVm;
    @Mock
    private AuditLogDirector mockAuditLogDirector;

    @Captor
    private ArgumentCaptor<AuditLogable> auditLogableCaptor;

    private DisplayNetworkClusterHelper underTest;

    @BeforeEach
    public void setUp() {
        underTest = new DisplayNetworkClusterHelper(
                mockNetworkClusterDao,
                mockVmDao,
                mockClusterDao,
                mockNetworkCluster,
                TEST_NETWORK_NAME,
                mockAuditLogDirector);

        when(mockNetworkCluster.getId()).thenReturn(TEST_NETWORK_CLUSTER_ID);
        when(mockNetworkClusterDao.get(TEST_NETWORK_CLUSTER_ID)).thenReturn(mockNetworkClusterBeforeUpdate);
        when(mockClusterDao.get(TEST_CLUSTER_ID)).thenReturn(createCluster(TEST_CLUSTER_NAME));
    }

    private Cluster createCluster(String clusterName) {
        final Cluster cluster = new Cluster();
        cluster.setName(clusterName);
        return cluster;
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
        when(mockNetworkCluster.isDisplay()).thenReturn(displayNetworkToBeSet);
        when(mockNetworkClusterBeforeUpdate.isDisplay()).thenReturn(displayNetworkBeforeUpdate);

        final boolean actual = underTest.isDisplayToBeUpdated();

        verify(mockNetworkCluster).getId();
        verify(mockNetworkClusterDao).get(TEST_NETWORK_CLUSTER_ID);
        verify(mockNetworkCluster).isDisplay();
        verify(mockNetworkClusterBeforeUpdate).isDisplay();

        assertEquals(expectedResult, actual);
    }

    /**
     * Test method for
     * {@link org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper#warnOnActiveVm()}.
     */
    @Test
    public void testWarnOnActiveVmPositive() {

        testWarnOnActiveVmInner(true);

        verify(mockAuditLogDirector).log(auditLogableCaptor.capture(),
                same(AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM));

        final AuditLogable actualLoggable = auditLogableCaptor.getValue();
        assertEquals(TEST_CLUSTER_NAME, actualLoggable.getClusterName());
        assertEquals(TEST_NETWORK_NAME, actualLoggable.getCustomValues().get("networkname"));
    }

    @Test
    public void testWarnOnActiveVmNegative() {

        testWarnOnActiveVmInner(false);

        verifyZeroInteractions(mockAuditLogDirector);
    }

    private void testWarnOnActiveVmInner(boolean activeVm) {
        final List<VM> clusterVms = Collections.singletonList(mockVm);

        when(mockNetworkCluster.getClusterId()).thenReturn(TEST_CLUSTER_ID);
        when(mockVmDao.getAllForCluster(TEST_CLUSTER_ID)).thenReturn(clusterVms);
        when(mockVm.isRunning()).thenReturn(activeVm);

        underTest.warnOnActiveVm();

        verify(mockNetworkCluster, atLeastOnce()).getClusterId();
        verify(mockVmDao).getAllForCluster(TEST_CLUSTER_ID);
        verify(mockVm).isRunning();
    }
}
