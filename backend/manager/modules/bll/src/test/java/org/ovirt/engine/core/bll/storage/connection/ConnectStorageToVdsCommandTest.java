package org.ovirt.engine.core.bll.storage.connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ConnectStorageToVdsCommandTest extends BaseCommandTest {

    private ConnectStorageToVdsCommand<StorageServerConnectionParametersBase> command;
    private StorageServerConnectionParametersBase params;

    @BeforeEach
    public void prepareCommand() {
        params = new StorageServerConnectionParametersBase();
        command = new ConnectStorageToVdsCommand<>(params, null);
    }

    private StorageServerConnections createConnection(StorageType storageType, String mountOptions) {
        StorageServerConnections conn = new StorageServerConnections();
        conn.setStorageType(storageType);
        conn.setMountOptions(mountOptions);
        return conn;
    }

    @Test
    public void testNfsConnectionWithInvalidMountOptionsNotLowerCase() {
        testConnectionMountOptionsValidations(StorageType.NFS, "TImeo=30, nfsVERS=4", false);
    }

    @Test
    public void testNfsConnectionWithInvalidMountOptions() {
        testConnectionMountOptionsValidations(StorageType.NFS, "timeo=30, nfsvers=4", false);
    }

    @Test
    public void testPosixConnectionWithInvalidMountOptions() {
        testConnectionMountOptionsValidations(StorageType.POSIXFS, "timeo=30, vfs_type=nfs", false);
    }

    @Test
    public void testPosixConnectionWithValidMountOptions() {
        testConnectionMountOptionsValidations(StorageType.POSIXFS, "timeo=30, validoption=666", true);
    }

    @Test
    public void testNfsConnectionWithValidMountOptions() {
        testConnectionMountOptionsValidations(StorageType.NFS, "validoption=30, anothervalidoption=666", true);
    }

    private void testConnectionMountOptionsValidations(StorageType storageType, String mountOptions, boolean shouldSucceed) {
        StorageServerConnections newPosixConnection = createConnection(storageType, mountOptions);
        params.setStorageServerConnection(newPosixConnection);
        ValidationResult result = command.validateMountOptions();
        if (shouldSucceed) {
            assertTrue(result.isValid());
        } else {
            assertThat(result, failsWith(EngineMessage.VALIDATION_STORAGE_CONNECTION_MOUNT_OPTIONS_CONTAINS_MANAGED_PROPERTY));
        }
    }
}
