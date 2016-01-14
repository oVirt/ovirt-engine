package org.ovirt.engine.core.bll.storage.connection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class AddStorageServerConnectionCommandTest extends StorageServerConnectionTestCommon {
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private AddStorageServerConnectionCommand<StorageServerConnectionParametersBase> command = null;

    @Mock
    StorageServerConnectionDao storageConnDao;

    @Mock
    StorageDomainDao storageDomainDao;

    @Before
    public void prepareParams() {
        parameters = new StorageServerConnectionParametersBase();
        parameters.setVdsId(Guid.newGuid());
        command = spy(new AddStorageServerConnectionCommand<>(parameters, null));
        doReturn(storageConnDao).when(command).getStorageConnDao();
        doReturn(storageDomainDao).when(command).getStorageDomainDao();
        doReturn(null).when(command).findConnectionWithSameDetails(any(StorageServerConnections.class));
    }

    @Test
    public void addPosixNonEmptyVFSType() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(false).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void addISCSINonEmptyIqn() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("10.35.16.25",
                        StorageType.ISCSI,
                        "iqn.2013-04.myhat.com:aaa-target1",
                        "3650",
                        "user1",
                        "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(false).when(command).isConnWithSameDetailsExists(newISCSIConnection, null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void addNFSEmptyConn() {
        StorageServerConnections newPosixConnection = createPosixConnection("", StorageType.POSIXFS, "nfs", "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
    }

    @Test
    public void addExistingConnection() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(true).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
    }

    @Test
    public void addNewConnectionWithVds() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        newPosixConnection.setId("");
        parameters.setStorageServerConnection(newPosixConnection);
        doReturn(false).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        Pair<Boolean, Integer> connectResult = new Pair(true, 0);
        doReturn(connectResult).when(command).connectHostToStorage();
        doReturn(null).when(command).getConnectionFromDbById(newPosixConnection.getId());
        doNothing().when(command).saveConnection(newPosixConnection);

        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void addNewConnectionEmptyVdsId() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        newPosixConnection.setId("");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(false).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        doReturn(null).when(command).getConnectionFromDbById(newPosixConnection.getId());
        doNothing().when(command).saveConnection(newPosixConnection);

        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void addNewConnectionNullVdsId() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        newPosixConnection.setId("");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(null);
        doReturn(false).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        doReturn(null).when(command).getConnectionFromDbById(newPosixConnection.getId());
        doNothing().when(command).saveConnection(newPosixConnection);

        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void addNotEmptyIdConnection() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        newPosixConnection.setId(Guid.newGuid().toString());
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(true).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_NOT_EMPTY);
    }


    @Test
    public void addISCSIEmptyConn() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("",
                        StorageType.ISCSI,
                        "iqn.2013-04.myhat.com:aaa-target1",
                        "3650",
                        "user1",
                        "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
    }

    @Test
    public void isConnWithSameDetailsExist() {
       StorageServerConnections  newISCSIConnection = createISCSIConnection("1.2.3.4", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3650", "user1", "mypassword123");
       StorageServerConnections  existingConn = createISCSIConnection("1.2.3.4", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3650", "user1", "mypassword123");
       existingConn.setId(Guid.newGuid().toString());

       when(command.findConnectionWithSameDetails(newISCSIConnection)).thenReturn(existingConn);
       boolean isExists = command.isConnWithSameDetailsExists(newISCSIConnection, null);
       assertTrue(isExists);
    }

    @Test
    public void isLocalDomainConnWithSamePathAndPoolExist() {
        StorageServerConnections newLocalConnection = populateBasicConnectionDetails(null, "/localSD", StorageType.LOCALFS);
        StorageServerConnections existingConn = populateBasicConnectionDetails(null, "/localSD", StorageType.LOCALFS);
        existingConn.setId(Guid.newGuid().toString());

        Guid storagePoolId = Guid.newGuid();
        List<StorageServerConnections> connections = Collections.singletonList(existingConn);

        when(storageConnDao.getAllConnectableStorageSeverConnection(storagePoolId)).thenReturn(connections);
        when(storageConnDao.getAllForStorage(newLocalConnection.getConnection())).thenReturn(connections);

        boolean isExists = command.isConnWithSameDetailsExists(newLocalConnection, storagePoolId);
        assertTrue(isExists);
    }

    @Test
    public void isLocalDomainConnWithSamePathAndPoolNotExist() {
        StorageServerConnections newLocalConnection = populateBasicConnectionDetails(null, "/localSD", StorageType.LOCALFS);
        StorageServerConnections existingConn = populateBasicConnectionDetails(null, "/localSD", StorageType.LOCALFS);

        Guid newLocalConnectionStoragePoolId = Guid.newGuid();
        Guid existingLocalConnectionStoragePoolId = Guid.newGuid();

        List<StorageServerConnections> connections = Collections.singletonList(existingConn);

        when(storageConnDao.getAllConnectableStorageSeverConnection(existingLocalConnectionStoragePoolId)).thenReturn(connections);
        when(storageConnDao.getAllForStorage(newLocalConnection.getConnection())).thenReturn(connections);

        boolean isExists = command.isConnWithSameDetailsExists(newLocalConnection, newLocalConnectionStoragePoolId);
        assertFalse(isExists);
    }

    @Override
    protected ConnectStorageToVdsCommand getCommand() {
        return command;
    }

    @Override
    protected boolean createConnectionWithId() {
        return false;
    }
}
