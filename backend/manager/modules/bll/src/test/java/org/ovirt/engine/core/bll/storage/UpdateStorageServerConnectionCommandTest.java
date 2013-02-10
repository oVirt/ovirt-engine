package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStorageServerConnectionCommandTest {

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase> command = null;

    private StorageServerConnections newConnection = null;
    private StorageServerConnections oldConnection = null;

    @Mock
    private StorageServerConnectionDAO storageConnDao;

    @Mock
    private StorageDomainDynamicDAO storageDomainDynamicDao;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    @Before
    public void prepareParams() {
        Guid id = Guid.NewGuid();
        newConnection =
                createConnection(id,
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

        StorageServerConnectionParametersBase parameters = new StorageServerConnectionParametersBase();
        parameters.setStorageServerConnection(newConnection);
        parameters.setVdsId(Guid.NewGuid());
        parameters.setStoragePoolId(Guid.NewGuid());
        command = spy(new UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase>(parameters));
        doReturn(storageConnDao).when(command).getStorageConnDao();
        doReturn(storageDomainDynamicDao).when(command).getStorageDomainDynamicDao();
        doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();

        oldConnection =
                createConnection(id,
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS,
                        NfsVersion.V4,
                        50,
                        0);
        when(storageConnDao.get(newConnection.getid())).thenReturn(oldConnection);
    }

    private StorageServerConnections createConnection(Guid id,
            String connection,
            StorageType type,
            NfsVersion version,
            int timeout,
            int retrans) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        connectionDetails.setid(id.toString());
        connectionDetails.setconnection(connection);
        connectionDetails.setNfsVersion(version);
        connectionDetails.setNfsTimeo((short) timeout);
        connectionDetails.setstorage_type(type);
        connectionDetails.setNfsRetrans((short) retrans);
        return connectionDetails;
    }

    @Test
    public void checkNoHost() {
        StorageServerConnectionParametersBase parameters = new StorageServerConnectionParametersBase();
        parameters.setStorageServerConnection(newConnection);
        parameters.setVdsId(null);
        parameters.setStoragePoolId(Guid.NewGuid());
        UpdateStorageServerConnectionCommand command =
                spy(new UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase>(parameters));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.VDS_EMPTY_NAME_OR_ID);
    }

    @Test
    public void checkEmptyIdHost() {
        StorageServerConnectionParametersBase parameters = new StorageServerConnectionParametersBase();
        parameters.setStorageServerConnection(newConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.NewGuid());
        UpdateStorageServerConnectionCommand command =
                spy(new UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase>(parameters));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.VDS_EMPTY_NAME_OR_ID);
    }

    @Test
    public void updateNonNFSConnection() {
        newConnection.setstorage_type(StorageType.ISCSI);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
    }

    @Test
    public void updateChangeConnectionType() {
        oldConnection.setstorage_type(StorageType.ISCSI);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
    }

    @Test
    public void updateNonExistingConnection() {
        when(storageConnDao.get(newConnection.getid())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void updateBadFormatPath() {
        newConnection.setconnection("host/mydir");
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_INVALID);
    }

    @Test
    public void updateSeveralConnectionsWithSamePath() {
        List<StorageServerConnections> connections = new ArrayList<StorageServerConnections>();
        StorageServerConnections conn1 = new StorageServerConnections();
        conn1.setconnection(newConnection.getconnection());
        conn1.setid(newConnection.getid());
        StorageServerConnections conn2 = new StorageServerConnections();
        conn2.setconnection(newConnection.getconnection());
        conn2.setid(Guid.NewGuid().toString());
        connections.add(conn1);
        connections.add(conn2);
        when(storageConnDao.getAllForStorage(newConnection.getconnection())).thenReturn(connections);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
    }

    @Test
    public void updateConnectionOfSeveralDomains() {
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageName("domain1");
        StorageDomain domain2 = new StorageDomain();
        domain2.setStorage(newConnection.getconnection());
        domain2.setStatus(StorageDomainStatus.Maintenance);
        domain2.setStorageName("domain2");
        domains.add(domain1);
        domains.add(domain2);
        doReturn(domains).when(command).getStorageDomainsByConnId(newConnection.getid());
        List<String> messages =
                CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
        assertTrue(messages.contains("$domainNames domain1,domain2"));
    }

    @Test
    public void updateConnectionOfActiveDomain() {
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domains.add(domain1);
        doReturn(domains).when(command).getStorageDomainsByConnId(newConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
    }

    @Test
    public void updateConnectionNoDomain() {
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        doReturn(domains).when(command).getStorageDomainsByConnId(newConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void succeedCanDoAction() {
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Maintenance);
        domains.add(domain1);
        doReturn(domains).when(command).getStorageDomainsByConnId(newConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void succeedUpdateCommand() {
        VDSReturnValue returnValueConnectSuccess = new VDSReturnValue();
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(map).when(command).getStoragePoolIsoMap();
        returnValueConnectSuccess.setSucceeded(true);
        StorageDomain domain = new StorageDomain();
        StorageDomainDynamic domainDynamic = new StorageDomainDynamic();
        domain.setStorageDynamicData(domainDynamic);
        returnValueConnectSuccess.setReturnValue(domain);
        doReturn(returnValueConnectSuccess).when(command).getStatsForDomain();
        doReturn(true).when(command).connectToStorage();
        doNothing().when(storageConnDao).update(newConnection);
        doNothing().when(storageDomainDynamicDao).update(domainDynamic);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Maintenance);
        doNothing().when(command).disconnectFromStorage();
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void failUpdateStats() {
        VDSReturnValue returnValueUpdate = new VDSReturnValue();
        returnValueUpdate.setSucceeded(false);
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(map).when(command).getStoragePoolIsoMap();
        doReturn(returnValueUpdate).when(command).getStatsForDomain();
        doReturn(true).when(command).connectToStorage();
        StorageDomainDynamic domainDynamic = new StorageDomainDynamic();
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Maintenance);
        doNothing().when(command).disconnectFromStorage();
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, false);
        verify(storageDomainDynamicDao, never()).update(domainDynamic);
        verify(storageConnDao, never()).update(newConnection);
    }

    @Test
    public void failUpdateConnectToStorage() {
        VDSReturnValue returnValueUpdate = new VDSReturnValue();
        returnValueUpdate.setSucceeded(true);
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(map).when(command).getStoragePoolIsoMap();
        doReturn(returnValueUpdate).when(command).getStatsForDomain();
        doReturn(false).when(command).connectToStorage();
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Maintenance);
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, false);
        StorageDomainDynamic domainDynamic = new StorageDomainDynamic();
        verify(storageDomainDynamicDao, never()).update(domainDynamic);
        verify(storageConnDao, never()).update(newConnection);
    }

}
