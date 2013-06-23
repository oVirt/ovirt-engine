package org.ovirt.engine.core.bll.storage;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class AddStorageServerConnectionCommandTest {
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private AddStorageServerConnectionCommand<StorageServerConnectionParametersBase> command = null;

    private StorageServerConnectionParametersBase parameters;

    @Before
    public void prepareParams() {
        parameters = new StorageServerConnectionParametersBase();
        parameters.setVdsId(Guid.NewGuid());
        parameters.setStoragePoolId(Guid.NewGuid());
        command = spy(new AddStorageServerConnectionCommand<StorageServerConnectionParametersBase>(parameters));
    }


     private StorageServerConnections createPosixConnection(String connection, StorageType type, String vfsType, String mountOptions) {
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(connection, type);
        connectionDetails.setVfsType(vfsType);
        connectionDetails.setMountOptions(mountOptions);
        return connectionDetails;
    }

    private StorageServerConnections createISCSIConnection(String connection, StorageType type, String iqn, String user, String password) {
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(connection, type);
        connectionDetails.setiqn(iqn);
        connectionDetails.setuser_name(user);
        connectionDetails.setpassword(password);
        return connectionDetails;
    }

    private StorageServerConnections populateBasicConnectionDetails(String connection, StorageType type) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        connectionDetails.setconnection(connection);
        connectionDetails.setstorage_type(type);
        return connectionDetails;
    }

    @Test
    public void addPosixEmptyVFSType() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        null,
                        "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setStoragePoolId(Guid.Empty);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE);
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
        parameters.setStoragePoolId(Guid.Empty);
        doReturn(false).when(command).isConnWithSameDetailsExists();
        doReturn(true).when(command).initializeVds();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

     @Test
     public void addISCSIEmptyIqn() {
        StorageServerConnections newISCSIConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI,"","user1","mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_IQN);
    }

     @Test
     public void addISCSINonEmptyIqn() {
        StorageServerConnections newISCSIConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI,"iqn.2013-04.myhat.com:aaa-target1","user1","mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        doReturn(false).when(command).isConnWithSameDetailsExists();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

     @Test
     public void addNFSEmptyConn() {
        StorageServerConnections newPosixConnection = createPosixConnection("",StorageType.POSIXFS, "nfs" , "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
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
        doReturn(true).when(command).initializeVds();
        doReturn(true).when(command).isConnWithSameDetailsExists();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
    }

    @Test
    public void addNewConnection() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        newPosixConnection.setid("");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        doReturn(false).when(command).isConnWithSameDetailsExists();
        Pair<Boolean, Integer> connectResult = new Pair(true, 0);
        doReturn(connectResult).when(command).connectHostToStorage();
        doReturn(null).when(command).getConnectionFromDbById(newPosixConnection.getid());
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
        newPosixConnection.setid(Guid.NewGuid().toString());
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        doReturn(true).when(command).isConnWithSameDetailsExists();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_NOT_EMPTY);
    }

    @Test
     public void addISCSIEmptyConn() {
        StorageServerConnections newISCSIConnection = createISCSIConnection("", StorageType.ISCSI,"iqn.2013-04.myhat.com:aaa-target1","user1","mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.Empty);
        doReturn(true).when(command).initializeVds();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
     }
}
