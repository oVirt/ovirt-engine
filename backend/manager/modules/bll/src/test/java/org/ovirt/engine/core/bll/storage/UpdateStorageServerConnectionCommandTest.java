package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
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
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStorageServerConnectionCommandTest {

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase> command = null;

    private StorageServerConnections oldNFSConnection = null;
    private StorageServerConnections oldPosixConnection = null;

    @Mock
    private StorageServerConnectionDAO storageConnDao;

    @Mock
    private StorageDomainDynamicDAO storageDomainDynamicDao;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAO;

    private StorageServerConnectionParametersBase parameters;

    @Before
    public void prepareParams() {

        oldNFSConnection =
                createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS,
                        NfsVersion.V4,
                        50,
                        0);

        oldPosixConnection =
                createPosixConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");

        prepareCommand();
    }

    private void prepareCommand() {
       parameters = new StorageServerConnectionParametersBase();
       parameters.setVdsId(Guid.newGuid());
       parameters.setStoragePoolId(Guid.newGuid());

       command = spy(new UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase>(parameters));
       doReturn(storageConnDao).when(command).getStorageConnDao();
       doReturn(storageDomainDynamicDao).when(command).getStorageDomainDynamicDao();
       doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();

    }

    private StorageServerConnections createNFSConnection(String connection,
                                                         StorageType type,
                                                         NfsVersion version,
                                                         int timeout,
                                                         int retrans) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setNfsVersion(version);
        connectionDetails.setNfsTimeo((short) timeout);
        connectionDetails.setNfsRetrans((short) retrans);
        return connectionDetails;
    }

    private StorageServerConnections createPosixConnection(String connection, StorageType type, String vfsType, String mountOptions) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setVfsType(vfsType);
        connectionDetails.setMountOptions(mountOptions);
        return connectionDetails;
    }

    private StorageServerConnections createISCSIConnection(String connection, StorageType type, String iqn, String user, String password) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setiqn(iqn);
        connectionDetails.setuser_name(user);
        connectionDetails.setpassword(password);
        return connectionDetails;
    }


    private StorageServerConnections populateBasicConnectionDetails(Guid id, String connection, StorageType type) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        connectionDetails.setid(id.toString());
        connectionDetails.setconnection(connection);
        connectionDetails.setstorage_type(type);
        return connectionDetails;
    }

    @Test
    public void checkNoHost() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        parameters.setVdsId(null);
        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.VDS_EMPTY_NAME_OR_ID);
    }

    @Test
    public void checkEmptyIdHost() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        parameters.setVdsId(Guid.Empty);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.VDS_EMPTY_NAME_OR_ID);
    }

    @Test
    public void updateIScsiConnection() {
          StorageServerConnections  newNFSConnection = createNFSConnection(
                  "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                  StorageType.ISCSI,
                  NfsVersion.V4,
                  300,
                  0);
        parameters.setStorageServerConnection(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
    }

    @Test
    public void updateChangeConnectionType() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        oldNFSConnection.setstorage_type(StorageType.ISCSI);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
    }

    @Test
    public void updateNonExistingConnection() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(null);
        parameters.setStorageServerConnection(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void updateBadFormatPath() {
         StorageServerConnections  newNFSConnection = createNFSConnection(
                        "host/mydir",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_INVALID);
    }


    @Test
    public void updatePosixEmptyVFSType() {
        StorageServerConnections newPosixConnection = createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1", StorageType.POSIXFS, null , "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE);
    }

    @Test
    public void updateSeveralConnectionsWithSamePath() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        List<StorageServerConnections> connections = new ArrayList<StorageServerConnections>();
        StorageServerConnections conn1 = new StorageServerConnections();
        conn1.setconnection(newNFSConnection.getconnection());
        conn1.setid(newNFSConnection.getid());
        StorageServerConnections conn2 = new StorageServerConnections();
        conn2.setconnection(newNFSConnection.getconnection());
        conn2.setid(Guid.newGuid().toString());
        connections.add(conn1);
        connections.add(conn2);
        when(storageConnDao.getAllForStorage(newNFSConnection.getconnection())).thenReturn(connections);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        doReturn(true).when(command).isConnWithSameDetailsExists(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
    }

    @Test
    public void updateConnectionOfSeveralDomains() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageName("domain1");
        StorageDomain domain2 = new StorageDomain();
        domain2.setStorage(newNFSConnection.getconnection());
        domain2.setStatus(StorageDomainStatus.Maintenance);
        domain2.setStorageName("domain2");
        domains.add(domain1);
        domains.add(domain2);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getid());
        doReturn(false).when(command).isConnWithSameDetailsExists(newNFSConnection);
        List<String> messages =
                CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
        assertTrue(messages.contains("$domainNames domain1,domain2"));
    }

    @Test
    public void updateConnectionOfActiveDomain() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domains.add(domain1);
        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getid());
        doReturn(false).when(command).isConnWithSameDetailsExists(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE);
    }

    @Test
    public void updateConnectionNoDomain() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void succeedCanDoActionNFS() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Maintenance);
        domains.add(domain1);
        when(storageConnDao.get(newNFSConnection.getid())).thenReturn(oldNFSConnection);
        doReturn(false).when(command).isConnWithSameDetailsExists(newNFSConnection);
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void succeedCanDoActionPosix() {
        StorageServerConnections newPosixConnection = createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1", StorageType.POSIXFS, "nfs", "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newPosixConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Maintenance);
        domains.add(domain1);
        parameters.setStorageServerConnection(newPosixConnection);
        when(storageConnDao.get(newPosixConnection.getid())).thenReturn(oldPosixConnection);
        doReturn(domains).when(command).getStorageDomainsByConnId(newPosixConnection.getid());
        doReturn(false).when(command).isConnWithSameDetailsExists(newPosixConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void succeedUpdateNFSCommandWithDomain() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
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
        doNothing().when(storageConnDao).update(newNFSConnection);
        doNothing().when(storageDomainDynamicDao).update(domainDynamic);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        doNothing().when(command).changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Maintenance);
        doNothing().when(command).disconnectFromStorage();
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void succeedUpdateNFSCommandNoDomain() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        VDSReturnValue returnValueConnectSuccess = new VDSReturnValue();
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(map).when(command).getStoragePoolIsoMap();
        doReturn(false).when(command).doDomainsUseConnection();
        returnValueConnectSuccess.setSucceeded(true);
        doReturn(true).when(command).connectToStorage();
        doNothing().when(storageConnDao).update(newNFSConnection);
        doNothing().when(command).disconnectFromStorage();
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void failUpdateStats() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        VDSReturnValue returnValueUpdate = new VDSReturnValue();
        returnValueUpdate.setSucceeded(false);
        doReturn(true).when(command).doDomainsUseConnection();
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
        verify(storageConnDao, never()).update(newNFSConnection);
    }

    @Test
    public void failUpdateConnectToStorage() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
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
        verify(storageConnDao, never()).update(newNFSConnection);
    }

    @Test
    public void isConnWithSameDetailsExistFileDomains() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       List<StorageServerConnections> connections = new ArrayList<>();
       StorageServerConnections connection1 = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
       connections.add(connection1);
       StorageServerConnections connection2 = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        600,
                        0);
       connections.add(connection2);

       when(storageConnDao.getAllForStorage(newNFSConnection.getconnection())).thenReturn(connections);
       boolean isExists = command.isConnWithSameDetailsExists(newNFSConnection);
       assertTrue(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistSameConnection() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       List<StorageServerConnections> connections = new ArrayList<>();
       StorageServerConnections connection1 = newNFSConnection;
       connections.add(connection1);

       when(storageConnDao.getAllForStorage(newNFSConnection.getconnection())).thenReturn(connections);
       boolean isExists = command.isConnWithSameDetailsExists(newNFSConnection);
       assertFalse(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistNoConnections() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       List<StorageServerConnections> connections = new ArrayList<>();

       when(storageConnDao.getAllForStorage(newNFSConnection.getconnection())).thenReturn(connections);
       boolean isExists = command.isConnWithSameDetailsExists(newNFSConnection);
       assertFalse(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistBlockDomains() {
       StorageServerConnections  newISCSIConnection = createISCSIConnection("1.2.3.4", StorageType.ISCSI,"iqn.2013-04.myhat.com:aaa-target1","user1","mypassword123");

       List<StorageServerConnections> connections = new ArrayList<>();
       StorageServerConnections connection1 = createISCSIConnection("1.2.3.4", StorageType.ISCSI,"iqn.2013-04.myhat.com:aaa-target1","user1","mypassword123");
       connections.add(connection1);

       when(storageConnDao.getAllForConnection(newISCSIConnection)).thenReturn(connections);
       boolean isExists = command.isConnWithSameDetailsExists(newISCSIConnection);
       assertTrue(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistCheckSameConn() {
       StorageServerConnections  newISCSIConnection = createISCSIConnection("1.2.3.4", StorageType.ISCSI,"iqn.2013-04.myhat.com:aaa-target1","user1","mypassword123");

       List<StorageServerConnections> connections = new ArrayList<>();
       connections.add(newISCSIConnection);

       when(storageConnDao.getAllForConnection(newISCSIConnection)).thenReturn(connections);
       boolean isExists = command.isConnWithSameDetailsExists(newISCSIConnection);
       assertFalse(isExists);
    }

}
