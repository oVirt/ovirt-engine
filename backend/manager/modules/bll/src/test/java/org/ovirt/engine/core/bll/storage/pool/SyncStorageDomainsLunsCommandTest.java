package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.SyncStorageDomainsLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class SyncStorageDomainsLunsCommandTest {

    private SyncStorageDomainsLunsParameters parameters =
            new SyncStorageDomainsLunsParameters(Guid.newGuid(), Collections.emptyList());

    @Spy
    private SyncStorageDomainsLunsCommand<SyncStorageDomainsLunsParameters> command =
            new SyncStorageDomainsLunsCommand<>(parameters, null);

    @Test
    public void validateInvalidVds() {
        doReturn(false).when(command).validateVds();
        assertFalse(command.validate());
    }

    @Test
    public void validateInvalidStorageType() {
        doReturn(true).when(command).validateVds();
        doReturn(false).when(command).validateStorageTypeOfStorageDomainsToSync();
        assertFalse(command.validate());
    }

    @Test
    public void validateStorageTypeOfStorageDomainsToSyncSucceeds() {
        StorageDomain blockDomain1 = new StorageDomain();
        blockDomain1.setStorageType(StorageType.ISCSI);

        StorageDomain blockDomain2 = new StorageDomain();
        blockDomain2.setStorageType(StorageType.FCP);

        doReturn(Stream.of(blockDomain1, blockDomain2)).when(command).getStorageDomainsToSync();
        assertTrue(command.validateStorageTypeOfStorageDomainsToSync());
    }

    @Test
    public void validateStorageTypeOfStorageDomainsToSyncFails() {
        StorageDomain blockDomain = new StorageDomain();
        blockDomain.setStorageType(StorageType.ISCSI);

        StorageDomain fileDomain = new StorageDomain();
        fileDomain.setStorageType(StorageType.NFS);

        doReturn(Stream.of(blockDomain, fileDomain)).when(command).getStorageDomainsToSync();
        assertFalse(command.validateStorageTypeOfStorageDomainsToSync());
    }
}
