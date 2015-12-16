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
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.springframework.dao.DataIntegrityViolationException;

public class AttachNetworkToClusterInternalCommandTest extends BaseCommandTest {

    /**
     * Since the command isn't in the same package as AuditLogableBase which defines the Dao accessors they
     * cannot be spied from here. Instead, will override them manually.
     */
    private class TestAttachNetworkToClusterCommand extends
                                                   AttachNetworkToClusterInternalCommand<AttachNetworkToVdsGroupParameter> {

        private TestAttachNetworkToClusterCommand(AttachNetworkToVdsGroupParameter parameters) {
            super(parameters);
        }

        @Override
        public VdsGroupDao getVdsGroupDao() {
            return mockVdsGroupDao;
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
    private VdsGroupDao mockVdsGroupDao;

    @Mock
    private NetworkDao mockNetworkDao;

    private AttachNetworkToClusterInternalCommand<AttachNetworkToVdsGroupParameter> underTest;

    private VDSGroup existingGroup = new VDSGroup();
    private Network network = createNetwork();
    private AttachNetworkToVdsGroupParameter param;

    @Before
    public void setup() {
        existingGroup.setCompatibilityVersion(Version.v3_1);
        param = new AttachNetworkToVdsGroupParameter(getExistingVdsGroup(), getNetwork());

        underTest = new TestAttachNetworkToClusterCommand(param);
    }

    @Test
    public void networkExists() {
        simulateVdsGroupExists();
        when(mockNetworkDao.get(any(Guid.class))).thenReturn(getNetwork());
        when(mockNetworkClusterDao.get(param.getNetworkCluster().getId())).thenReturn(param.getNetworkCluster());
        assertValidateFailure(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_CLUSTER.toString());
    }

    @Test
    public void networkDoesntExist() {
        simulateVdsGroupExists();
        assertValidateFailure(EngineMessage.NETWORK_NOT_EXISTS.toString());
    }

    @Test
    public void noVdsGroup() {
        simulateVdsGroupDoesNotExist();
        assertValidateFailure(EngineMessage.VDS_CLUSTER_IS_NOT_VALID.toString());
    }

    @Test
    public void raceConditionVdsGroupRemoved() {
        simulateVdsGroupExists();
        simulateVdsGroupWasRemoved();
        assertExecuteActionFailure();
    }

    private Network createNetwork() {
        network = new Network();
        network.setName("test network");
        return network;
    }

    private void simulateVdsGroupExists() {
        dbFacadeReturnVdsGroup();
    }

    private void simulateVdsGroupDoesNotExist() {
        dbFacadeReturnNoVdsGroup();
    }

    private void simulateVdsGroupWasRemoved() {
        dbFacadeThrowOnNetworkClusterSave();
    }

    private void dbFacadeReturnNoVdsGroup() {
        when(mockVdsGroupDao.get(any(Guid.class))).thenReturn(null);
    }

    private void dbFacadeReturnVdsGroup() {
        when(mockVdsGroupDao.get(any(Guid.class))).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        doThrow(new DataIntegrityViolationException("test violations")).when(mockNetworkClusterDao)
                .save(Matchers.any(NetworkCluster.class));
    }

    private Network getNetwork() {
        return network;
    }

    private VDSGroup getExistingVdsGroup() {
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
        assertEquals(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED, underTest.getAuditLogTypeValue());
    }
}
