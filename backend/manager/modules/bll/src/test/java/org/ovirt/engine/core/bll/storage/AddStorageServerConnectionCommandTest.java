package org.ovirt.engine.core.bll.storage;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
        Guid id = Guid.NewGuid();
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
     public void updatePosixEmptyVFSType() {
        StorageServerConnections newPosixConnection = createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1", StorageType.POSIXFS, null , "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setStoragePoolId(Guid.Empty);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE);
    }

    @Test
     public void updatePosixNonEmptyVFSType() {
        StorageServerConnections newPosixConnection = createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1", StorageType.POSIXFS, "nfs" , "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        parameters.setStoragePoolId(Guid.Empty);
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
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }
}
