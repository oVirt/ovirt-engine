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
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setVfsType(vfsType);
        connectionDetails.setMountOptions(mountOptions);
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
        doReturn(true).when(command).InitializeVds();
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }


}
