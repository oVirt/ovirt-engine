package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.springframework.dao.DataIntegrityViolationException;

@RunWith(MockitoJUnitRunner.class)
public class AttachNetworkToVdsGroupCommandTest {

    private VDSGroup existingGroup = new VDSGroup();
    private Network network = createNetwork();
    private AttachNetworkToVdsGroupParameter param;
    private AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter> command;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.DefaultManagementNetwork, "mgmt"));

    @Mock
    NetworkClusterDao networkClusterDAO;

    @Mock
    VdsGroupDAO vdsGroupDAO;

    @Mock
    NetworkDao networkDao;

    @Before
    public void setup() {
        existingGroup.setCompatibilityVersion(Version.v3_1);
        createCommand();
    }

    @Test
    public void networkExists() {
        simulateVdsGroupExists();
        when(networkDao.get(any(Guid.class))).thenReturn(getNetwork());
        when(networkClusterDAO.get(param.getNetworkCluster().getId())).thenReturn(param.getNetworkCluster());
        assertCanDoActionFailure(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_CLUSTER.toString());
    }

    @Test
    public void networkDoesntExist() {
        simulateVdsGroupExists();
        assertCanDoActionFailure(VdcBllMessages.NETWORK_NOT_EXISTS.toString());
    }

    @Test
    public void noVdsGroup() {
        simulateVdsGroupDoesNotExist();
        assertCanDoActionFailure(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID.toString());
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

    @SuppressWarnings("serial")
    public void createCommand() {
        param = new AttachNetworkToVdsGroupParameter(getExistingVdsGroupId(), getNetwork());

        command = new AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter>(param) {
            // Since the command isn't in the same package as AuditLogableBase which defines the DAO accessors they
            // cannot be spied from here.
            // Instead, will override them manually.

            @Override
            public VdsGroupDAO getVdsGroupDAO() {
                return vdsGroupDAO;
            }

            @Override
            protected NetworkClusterDao getNetworkClusterDAO() {
                return networkClusterDAO;
            }

            @Override
            protected NetworkDao getNetworkDAO() {
                return networkDao;
            }
        };
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
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(null);
    }

    private void dbFacadeReturnVdsGroup() {
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        doThrow(new DataIntegrityViolationException("test violations")).when(networkClusterDAO)
                .save(Matchers.<NetworkCluster> any(NetworkCluster.class));
    }

    private Network getNetwork() {
        return network;
    }

    private VDSGroup getExistingVdsGroupId() {
        return existingGroup;
    }

    private void assertCanDoActionFailure(final String messageToVerify) {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(messageToVerify));
    }

    private void assertExecuteActionFailure() {
        try {
            command.executeCommand();
        } catch (Exception expected) {
            // An exception is expected here
        }

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED, command.getAuditLogTypeValue());
    }
}
