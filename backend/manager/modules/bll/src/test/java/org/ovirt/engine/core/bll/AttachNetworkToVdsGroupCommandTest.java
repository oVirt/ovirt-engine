package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.NetworkClusterDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.dao.DataIntegrityViolationException;

@PrepareForTest({DbFacade.class})
@RunWith(PowerMockRunner.class)
public class AttachNetworkToVdsGroupCommandTest {

    private VDSGroup existingGroup = new VDSGroup();
    private network network = createNetwork();
    private DbFacade dbFacade = mock(DbFacade.class);
    private AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter> command;


    public AttachNetworkToVdsGroupCommandTest() {
        mockStatic(DbFacade.class);
    }

    @Before
    public void setup() {
        existingGroup.setcompatibility_version(Version.v3_1);
        createCommand();
        when(DbFacade.getInstance()).thenReturn(getDbFacadeMock());
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

    public void createCommand() {
        AttachNetworkToVdsGroupParameter param = new AttachNetworkToVdsGroupParameter(getExistingVdsGroupId(), getNetwork());
        command = new AttachNetworkToVdsGroupCommand<AttachNetworkToVdsGroupParameter>(param);
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
        VdsGroupDAO dao = mock(VdsGroupDAO.class);
        when(getDbFacadeMock().getVdsGroupDAO()).thenReturn(dao);
        when(dao.get(any(Guid.class))).thenReturn(null);
    }

    private void dbFacadeReturnNetworkListFromDb() {
        NetworkDAO dao = mock(NetworkDAO.class);
        when(getDbFacadeMock().getNetworkDAO()).thenReturn(dao);
        when(dao.getAllForCluster(any(Guid.class))).thenReturn(getNetworkList());
    }

    private void dbFacadeReturnEmptyNetworkList() {
        NetworkDAO dao = mock(NetworkDAO.class);
        when(getDbFacadeMock().getNetworkDAO()).thenReturn(dao);
        when(dao.getAllForCluster(any(Guid.class))).thenReturn(new ArrayList<network>());
    }

    private void dbFacadeReturnVdsGroup() {
        VdsGroupDAO dao = mock(VdsGroupDAO.class);
        when(getDbFacadeMock().getVdsGroupDAO()).thenReturn(dao);
        when(dao.get(any(Guid.class))).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        NetworkClusterDAO dao = mock(NetworkClusterDAO.class);
        when(getDbFacadeMock().getNetworkClusterDAO()).thenReturn(dao);
        doThrow(new DataIntegrityViolationException("test violations")).when(dao).save(Matchers.<network_cluster>any(network_cluster.class));
    }

    private DbFacade getDbFacadeMock() {
        return dbFacade;
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
        } catch (Exception e) {

        }
        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED, command.getAuditLogTypeValue());
    }
}
