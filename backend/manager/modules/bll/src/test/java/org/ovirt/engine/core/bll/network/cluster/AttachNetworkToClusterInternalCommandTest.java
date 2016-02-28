package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.springframework.dao.DataIntegrityViolationException;

public class AttachNetworkToClusterInternalCommandTest extends BaseCommandTest {

    /**
     * Since the command isn't in the same package as AuditLogableBase which defines the Dao accessors they
     * cannot be spied from here. Instead, will override them manually.
     */
    private class TestAttachNetworkToClusterCommand extends
                                                   AttachNetworkToClusterInternalCommand<AttachNetworkToClusterParameter> {

        private TestAttachNetworkToClusterCommand(AttachNetworkToClusterParameter parameters) {
            super(parameters, null);
        }

        @Override
        public ClusterDao getClusterDao() {
            return mockClusterDao;
        }

        @Override
        protected NetworkClusterDao getNetworkClusterDao() {
            return mockNetworkClusterDao;
        }

        @Override
        protected NetworkDao getNetworkDao() {
            return mockNetworkDao;
        }
    }

    @Mock
    private NetworkClusterDao mockNetworkClusterDao;

    @Mock
    private ClusterDao mockClusterDao;

    @Mock
    private NetworkDao mockNetworkDao;

    private AttachNetworkToClusterInternalCommand<AttachNetworkToClusterParameter> underTest;

    private Cluster existingGroup = new Cluster();
    private Network network = createNetwork();
    private AttachNetworkToClusterParameter param;

    @Before
    public void setup() {
        param = new AttachNetworkToClusterParameter(getExistingCluster(), getNetwork());

        underTest = new TestAttachNetworkToClusterCommand(param);
    }

    @Test
    public void networkExists() {
        simulateClusterExists();
        when(mockNetworkDao.get(any(Guid.class))).thenReturn(getNetwork());
        when(mockNetworkClusterDao.get(param.getNetworkCluster().getId())).thenReturn(param.getNetworkCluster());
        assertValidateFailure(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_CLUSTER.toString());
    }

    @Test
    public void networkDoesntExist() {
        simulateClusterExists();
        assertValidateFailure(EngineMessage.NETWORK_NOT_EXISTS.toString());
    }

    @Test
    public void noCluster() {
        simulateClusterDoesNotExist();
        assertValidateFailure(EngineMessage.VDS_CLUSTER_IS_NOT_VALID.toString());
    }

    @Test
    public void raceConditionClusterRemoved() {
        simulateClusterExists();
        simulateClusterWasRemoved();
        assertExecuteActionFailure();
    }

    private Network createNetwork() {
        network = new Network();
        network.setName("test network");
        return network;
    }

    private void simulateClusterExists() {
        dbFacadeReturnCluster();
    }

    private void simulateClusterDoesNotExist() {
        dbFacadeReturnNoCluster();
    }

    private void simulateClusterWasRemoved() {
        dbFacadeThrowOnNetworkClusterSave();
    }

    private void dbFacadeReturnNoCluster() {
        when(mockClusterDao.get(any(Guid.class))).thenReturn(null);
    }

    private void dbFacadeReturnCluster() {
        when(mockClusterDao.get(any(Guid.class))).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        doThrow(new DataIntegrityViolationException("test violations")).when(mockNetworkClusterDao)
                .save(Matchers.any(NetworkCluster.class));
    }

    private Network getNetwork() {
        return network;
    }

    private Cluster getExistingCluster() {
        return existingGroup;
    }

    private void assertValidateFailure(final String messageToVerify) {
        assertFalse(underTest.validate());
        assertTrue(underTest.getReturnValue().getValidationMessages().contains(messageToVerify));
    }

    private void assertExecuteActionFailure() {
        try {
            underTest.executeCommand();
        } catch (Exception expected) {
            // An exception is expected here
        }

        assertFalse(underTest.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.NETWORK_ATTACH_NETWORK_TO_CLUSTER_FAILED, underTest.getAuditLogTypeValue());
    }
}
