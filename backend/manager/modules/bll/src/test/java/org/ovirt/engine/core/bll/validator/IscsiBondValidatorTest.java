package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
public class IscsiBondValidatorTest {
    @Mock
    private IscsiBondDao iscsiBondDao;
    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkClusterDao networkClusterDao;
    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;

    @InjectMocks
    private IscsiBondValidator validator;

    @Test
    public void iscsiBondExists() {
        assertEquals(ValidationResult.VALID, validator.isIscsiBondExist(new IscsiBond()));
    }

    @Test
    public void iscsiBondDoesNotExist() {
        ValidationResult res = validator.isIscsiBondExist(null);
        assertThat(res, failsWith(EngineMessage.ISCSI_BOND_NOT_EXIST));
    }

    @Test
    public void iscsiBondWithTheSameNameExistsInDataCenter() {
        List<IscsiBond> iscsiBonds = new ArrayList<>();
        Guid dataCenterId = Guid.newGuid();

        iscsiBonds.add(createIscsiBond("First", dataCenterId));
        iscsiBonds.add(createIscsiBond("Second", dataCenterId));
        doReturn(iscsiBonds).when(iscsiBondDao).getAllByStoragePoolId(any());

        ValidationResult res = validator.iscsiBondWithTheSameNameExistInDataCenter(createIscsiBond("Second", dataCenterId));
        assertThat(res, failsWith(EngineMessage.ISCSI_BOND_WITH_SAME_NAME_EXIST_IN_DATA_CENTER));
    }

    @Test
    public void iscsiBondWithTheSameNameDoesNotExistInDataCenter() {
        List<IscsiBond> iscsiBonds = new ArrayList<>();
        Guid dataCenterId = Guid.newGuid();

        iscsiBonds.add(createIscsiBond("First", dataCenterId));
        iscsiBonds.add(createIscsiBond("Second", dataCenterId));
        doReturn(iscsiBonds).when(iscsiBondDao).getAllByStoragePoolId(any());

        assertEquals(ValidationResult.VALID,
                validator.iscsiBondWithTheSameNameExistInDataCenter(createIscsiBond("Third", dataCenterId)));
    }

    @Test
    public void addedLogicalNetworkBelongToAnotherDatacenter() {
        IscsiBond iscsiBond = createIscsiBond("First", Guid.newGuid());

        List<Network> networks = new ArrayList<>();
        networks.add(createNetwork(iscsiBond.getStoragePoolId()));
        doReturn(networks).when(networkDao).getAllForDataCenter(iscsiBond.getStoragePoolId());

        iscsiBond.getNetworkIds().add(networks.get(0).getId());
        iscsiBond.getNetworkIds().add(Guid.newGuid());

        ValidationResult res = validator.validateAddedLogicalNetworks(iscsiBond);

        assertThat(res, failsWith(EngineMessage.NETWORKS_DONT_EXIST_IN_DATA_CENTER));
        assertEquals(2, res.getVariableReplacements().size());
        assertEquals("$networkIds " + iscsiBond.getNetworkIds().get(1).toString(), res.getVariableReplacements().get(0));
        assertEquals("$dataCenterId " + iscsiBond.getStoragePoolId().toString(), res.getVariableReplacements().get(1));
    }

    @Test
    public void addedLogicalNetworkBelongToAnotherDatacenter2() {
        Guid dataCenterId = Guid.newGuid();
        IscsiBond before = createIscsiBond("Before", dataCenterId);
        IscsiBond after = createIscsiBond("After", dataCenterId);

        List<Network> networks = new ArrayList<>();
        networks.add(createNetwork(dataCenterId));
        doReturn(networks).when(networkDao).getAllForDataCenter(dataCenterId);

        before.getNetworkIds().add(networks.get(0).getId());
        after.getNetworkIds().add(networks.get(0).getId());
        after.getNetworkIds().add(Guid.newGuid());

        ValidationResult res = validator.validateAddedLogicalNetworks(after, before);

        assertThat(res, failsWith(EngineMessage.NETWORKS_DONT_EXIST_IN_DATA_CENTER));
        assertEquals(2, res.getVariableReplacements().size());
        assertEquals("$networkIds " + after.getNetworkIds().get(1).toString(), res.getVariableReplacements().get(0));
        assertEquals("$dataCenterId " + after.getStoragePoolId().toString(), res.getVariableReplacements().get(1));
    }

    @Test
    public void addedLogicalNetworkBelongToSameDatacenter() {
        IscsiBond iscsiBond = createIscsiBond("First", Guid.newGuid());

        List<Network> networks = new ArrayList<>();
        networks.add(createNetwork(iscsiBond.getStoragePoolId()));
        networks.add(createNetwork(iscsiBond.getStoragePoolId()));
        doReturn(networks).when(networkDao).getAllForDataCenter(iscsiBond.getStoragePoolId());

        for (Network network : networks) {
            iscsiBond.getNetworkIds().add(network.getId());
        }

        List<NetworkCluster> networkClusters = new ArrayList<>();
        networkClusters.add(createNetworkCluster(false));
        doReturn(networkClusters).when(networkClusterDao).getAllForNetwork(any());

        assertEquals(ValidationResult.VALID, validator.validateAddedLogicalNetworks(iscsiBond));
    }

    @Test
    public void addedRequiredLogicalNetworks() {
        IscsiBond iscsiBond = createIscsiBond("First", Guid.newGuid());

        List<Network> networks = new ArrayList<>();
        networks.add(createNetwork(iscsiBond.getStoragePoolId()));
        networks.add(createNetwork(iscsiBond.getStoragePoolId()));
        doReturn(networks).when(networkDao).getAllForDataCenter(iscsiBond.getStoragePoolId());

        for (Network network : networks) {
            iscsiBond.getNetworkIds().add(network.getId());
        }

        List<NetworkCluster> networkClusters = new ArrayList<>();
        networkClusters.add(createNetworkCluster(true));
        doReturn(networkClusters).when(networkClusterDao).getAllForNetwork(any());

        ValidationResult res = validator.validateAddedLogicalNetworks(iscsiBond);

        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_FAILED_ISCSI_BOND_NETWORK_CANNOT_BE_REQUIRED));
    }

    @Test
    public void successfullyAddedStorageConnections() {
        IscsiBond iscsiBond = createIscsiBond("First", Guid.newGuid());

        List<StorageServerConnections> conns = new ArrayList<>();
        conns.add(createStorageConnection());
        conns.add(createStorageConnection());
        doReturn(conns).when(storageServerConnectionDao).getConnectableStorageConnectionsByStorageType(iscsiBond.getStoragePoolId(), StorageType.ISCSI);

        iscsiBond.getStorageConnectionIds().add(conns.get(0).getId());
        iscsiBond.getStorageConnectionIds().add(conns.get(1).getId());

        assertEquals(ValidationResult.VALID, validator.validateAddedStorageConnections(iscsiBond));
    }

    @Test
    public void someAddedStorageConnectionsAreNotAnIscsi() {
        IscsiBond iscsiBond = createIscsiBond("First", Guid.newGuid());

        List<StorageServerConnections> conns = new ArrayList<>();
        conns.add(createStorageConnection());
        doReturn(conns).when(storageServerConnectionDao).getConnectableStorageConnectionsByStorageType(iscsiBond.getStoragePoolId(), StorageType.ISCSI);

        iscsiBond.getStorageConnectionIds().add(conns.get(0).getId());
        iscsiBond.getStorageConnectionIds().add(Guid.newGuid().toString());

        ValidationResult res = validator.validateAddedStorageConnections(iscsiBond);

        assertThat(res, failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTIONS_CANNOT_BE_ADDED_TO_ISCSI_BOND));
        assertEquals(1, res.getVariableReplacements().size());
        assertEquals("$connectionIds " + iscsiBond.getStorageConnectionIds().get(1).toString(), res.getVariableReplacements().get(0));
    }

    private IscsiBond createIscsiBond(String name, Guid dataCenterId) {
        IscsiBond iscsiBond = new IscsiBond();
        iscsiBond.setId(Guid.newGuid());
        iscsiBond.setStoragePoolId(dataCenterId);
        iscsiBond.setName(name);
        return iscsiBond;
    }

    private Network createNetwork(Guid dataCenterId) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setDataCenterId(dataCenterId);
        return network;
    }

    private NetworkCluster createNetworkCluster(boolean isRequired) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setRequired(isRequired);
        return networkCluster;
    }

    private StorageServerConnections createStorageConnection() {
        StorageServerConnections conn = new StorageServerConnections();
        conn.setId(Guid.newGuid().toString());
        return conn;
    }
}
