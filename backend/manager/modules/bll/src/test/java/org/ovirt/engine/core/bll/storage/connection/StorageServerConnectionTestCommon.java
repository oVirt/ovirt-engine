package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageServerConnectionTestCommon extends BaseCommandTest {

    protected StorageServerConnectionParametersBase parameters;

    private Guid getConnectionId() {
        return createConnectionWithId() ? Guid.newGuid() : null;
    }

    protected StorageServerConnections createNFSConnection(String connection,
                                                         StorageType type,
                                                         NfsVersion version,
                                                         int timeout,
                                                         int retrans) {
        Guid id = getConnectionId();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setNfsVersion(version);
        connectionDetails.setNfsTimeo((short) timeout);
        connectionDetails.setNfsRetrans((short) retrans);
        return connectionDetails;
    }

    protected StorageServerConnections createPosixConnection(String connection,
                                                           StorageType type,
                                                           String vfsType,
                                                           String mountOptions) {
        Guid id = getConnectionId();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setVfsType(vfsType);
        connectionDetails.setMountOptions(mountOptions);
        return connectionDetails;
    }

    protected StorageServerConnections populateBasicConnectionDetails(Guid id, String connection, StorageType type) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        if (id != null) {
            connectionDetails.setId(id.toString());
        }
        connectionDetails.setConnection(connection);
        connectionDetails.setStorageType(type);
        return connectionDetails;
    }

    protected StorageServerConnections createISCSIConnection(String connection,
                                                           StorageType type,
                                                           String iqn,
                                                           String port,
                                                           String user,
                                                           String password) {
        Guid id = getConnectionId();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setIqn(iqn);
        connectionDetails.setPort(port);
        connectionDetails.setUserName(user);
        connectionDetails.setPassword(password);
        return connectionDetails;
    }

    @Test
    public void testISCSIEmptyIqn() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("10.35.16.25", StorageType.ISCSI, "", "3650", "user1", "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_IQN);
    }

    @Test
    public void testISCSIEmptyPort() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "", "user1", "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID_PORT);
    }

    @Test
    public void testISCSIInvalidPort() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "-3650", "user1", "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID_PORT);
    }

    @Test
    public void testISCSIInvalidPortWithCharacters() {
        StorageServerConnections newISCSIConnection =
                createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "abc", "user1", "mypassword123");
        parameters.setStorageServerConnection(newISCSIConnection);
        parameters.setVdsId(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID_PORT);
    }

    @Test
    public void testPosixEmptyVFSType() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        null,
                        "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE);
    }

    @Test
    public void testConnectionWithInvalidMountOptionsFails() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS, "nfs", "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(new ValidationResult(EngineMessage.VALIDATION_STORAGE_CONNECTION_MOUNT_OPTIONS_CONTAINS_MANAGED_PROPERTY)).when(getCommand()).validateMountOptions();
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(), EngineMessage.VALIDATION_STORAGE_CONNECTION_MOUNT_OPTIONS_CONTAINS_MANAGED_PROPERTY);
    }

    @Test
    public void testConnectionWithValidMountOptionsSucceeds() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS, "nfs", "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);
        parameters.setVdsId(Guid.Empty);
        doReturn(ValidationResult.VALID).when(getCommand()).validateMountOptions();
        when(getCommand().getStorageConnDao().get(newPosixConnection.getId())).thenReturn(newPosixConnection);
        ValidateTestUtils.runAndAssertValidateSuccess(getCommand());
    }

    protected abstract ConnectStorageToVdsCommand getCommand();
    protected abstract boolean createConnectionWithId();
}
