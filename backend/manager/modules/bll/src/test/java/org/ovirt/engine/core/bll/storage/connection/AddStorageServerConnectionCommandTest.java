package org.ovirt.engine.core.bll.storage.connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddStorageServerConnectionCommandTest extends
        StorageServerConnectionTestCommon<AddStorageServerConnectionCommand<StorageServerConnectionParametersBase>> {

    @Mock
    protected ISCSIStorageHelper iscsiStorageHelper;

    @Override
    protected AddStorageServerConnectionCommand<StorageServerConnectionParametersBase> createCommand() {
        parameters = new StorageServerConnectionParametersBase();
        parameters.setVdsId(Guid.newGuid());
        return new AddStorageServerConnectionCommand<>(parameters, null);
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
        doReturn("").when(command).isConnWithSameDetailsExists(newPosixConnection, null);
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
        doReturn("").when(command).isConnWithSameDetailsExists(newISCSIConnection, null);
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
        String guid = Guid.newGuid().toString();
        doReturn(guid).when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        doReturn("storage_domain_01").when(command).getStorageNameByConnectionId(guid);
        List<String> messages = ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
        assertTrue(messages.contains("$connectionId " + guid) && messages.contains("$storageDomainName storage_domain_01"));
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
        doReturn("").when(command).isConnWithSameDetailsExists(newPosixConnection, null);
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
        doReturn("").when(command).isConnWithSameDetailsExists(newPosixConnection, null);
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
        doReturn("").when(command).isConnWithSameDetailsExists(newPosixConnection, null);
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

       when(iscsiStorageHelper.findConnectionWithSameDetails(newISCSIConnection)).thenReturn(existingConn);
       String isExists = command.isConnWithSameDetailsExists(newISCSIConnection, null);
       assertFalse(isExists.isEmpty());
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

        String isExists = command.isConnWithSameDetailsExists(newLocalConnection, storagePoolId);
        assertFalse(isExists.isEmpty());
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

        String isExists = command.isConnWithSameDetailsExists(newLocalConnection, newLocalConnectionStoragePoolId);
        assertTrue(isExists.isEmpty());
    }

    @Override
    protected boolean createConnectionWithId() {
        return false;
    }
}
