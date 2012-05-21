package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.NetworkClusterDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.springframework.dao.DataIntegrityViolationException;

@RunWith(MockitoJUnitRunner.class)
public class AttachNetworkToVdsGroupCommandTest {

    private VDSGroup existingGroup = new VDSGroup();
    private network network = createNetwork();
    private AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter> command;

    @Mock
    NetworkClusterDAO networkClusterDAO;

    @Mock
    NetworkDAO networkDAO;

    @Mock
    VdsGroupDAO vdsGroupDAO;

    @Before
    public void setup() {
        existingGroup.setcompatibility_version(Version.v3_1);
        createCommand();
    }

    @Test
    public void networkExists() {
        simulateNetworkAlreadyExists();
        simulateVdsGroupExists();
        assertCanDoActionSucceeds();
    }

    @Test
    public void newNetwork() {
        simulateNetworkDoesNotExist();
        simulateVdsGroupExists();
        assertCanDoActionSucceeds();
    }

    @Test
    public void noVdsGroup() {
        simulateNetworkDoesNotExist();
        simulateVdsGroupDoesNotExist();
        assertCanDoActionFailure(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID.toString());
    }

    @Test
    public void raceConditionVdsGroupRemoved() {
        simulateVdsGroupExists();
        simulateVdsGroupWasRemoved();
        assertExecuteActionFailure();
    }

    private network createNetwork() {
        network = new network();
        network.setname("test network");
        return network;
    }

    @SuppressWarnings("serial")
    public void createCommand() {
        AttachNetworkToVdsGroupParameter param =
                new AttachNetworkToVdsGroupParameter(getExistingVdsGroupId(), getNetwork());

        command = new AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter>(param) {
            // Since the command isn't in the same package as AuditLogableBase which defines the DAO accessors they
            // cannot be spied from here.
            // Instead, will override them manually.

            @Override
            protected VdsGroupDAO getVdsGroupDAO() {
                return vdsGroupDAO;
            }

            @Override
            protected NetworkClusterDAO getNetworkClusterDAO() {
                return networkClusterDAO;
            }

            @Override
            protected NetworkDAO getNetworkDAO() {
                return networkDAO;
            }
        };
    }

    private void simulateNetworkAlreadyExists() {
        dbFacadeReturnNetworkListFromDb();
    }

    private void simulateNetworkDoesNotExist() {
        dbFacadeReturnEmptyNetworkList();
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

    private void dbFacadeReturnNetworkListFromDb() {
        when(networkDAO.getAllForCluster(any(Guid.class))).thenReturn(getNetworkList());
    }

    private void dbFacadeReturnEmptyNetworkList() {
        when(networkDAO.getAllForCluster(any(Guid.class))).thenReturn(new ArrayList<network>());
    }

    private void dbFacadeReturnVdsGroup() {
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        doThrow(new DataIntegrityViolationException("test violations")).when(networkClusterDAO)
                .save(Matchers.<network_cluster> any(network_cluster.class));
    }

    private network getNetwork() {
        return network;
    }

    private VDSGroup getExistingVdsGroupId() {
        return existingGroup;
    }

    private List<network> getNetworkList() {
        ArrayList<network> list = new ArrayList<network>();
        list.add(network);
        return list;
    }

    private void assertCanDoActionFailure(final String messageToVerify) {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(messageToVerify));
    }

    private void assertCanDoActionSucceeds() {
        assertTrue(command.canDoAction());
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
