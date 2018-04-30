package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.springframework.dao.DataIntegrityViolationException;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AttachNetworkToClusterInternalCommandTest extends BaseCommandTest {
    @Mock
    private NetworkClusterDao mockNetworkClusterDao;

    @Mock
    private ClusterDao mockClusterDao;

    @Mock
    private NetworkDao mockNetworkDao;

    private Cluster existingGroup = new Cluster();
    private Network network = createNetwork();

    private AttachNetworkToClusterParameter param =
            new AttachNetworkToClusterParameter(getExistingCluster(), getNetwork());

    @InjectMocks
    private AttachNetworkToClusterInternalCommand<AttachNetworkToClusterParameter> underTest =
            new AttachNetworkToClusterInternalCommand<>(param, null);

    @Test
    public void networkExists() {
        simulateClusterExists();
        when(mockNetworkDao.get(any())).thenReturn(getNetwork());
        when(mockNetworkClusterDao.get(param.getNetworkCluster().getId())).thenReturn(param.getNetworkCluster());
        ValidateTestUtils.runAndAssertValidateFailure(underTest, EngineMessage.NETWORK_ALREADY_ATTACHED_TO_CLUSTER);
    }

    @Test
    public void networkDoesntExist() {
        simulateClusterExists();
        ValidateTestUtils.runAndAssertValidateFailure(underTest, EngineMessage.NETWORK_NOT_EXISTS);
    }

    @Test
    public void noCluster() {
        ValidateTestUtils.runAndAssertValidateFailure(underTest, EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
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

    private void simulateClusterWasRemoved() {
        dbFacadeThrowOnNetworkClusterSave();
    }

    private void dbFacadeReturnCluster() {
        when(mockClusterDao.get(any())).thenReturn(existingGroup);
    }

    private void dbFacadeThrowOnNetworkClusterSave() {
        doThrow(new DataIntegrityViolationException("test violations")).when(mockNetworkClusterDao).save(any());
    }

    private Network getNetwork() {
        return network;
    }

    private Cluster getExistingCluster() {
        return existingGroup;
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
