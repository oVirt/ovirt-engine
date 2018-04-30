package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class UpdateOvfStoreForStorageDomainCommandTest extends BaseCommandTest {
    private StorageDomainParametersBase params = new StorageDomainParametersBase(Guid.newGuid());

    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StoragePoolDao storagePoolDao;

    @InjectMocks
    private UpdateOvfStoreForStorageDomainCommand<StorageDomainParametersBase> cmd =
            new UpdateOvfStoreForStorageDomainCommand<>(params, CommandContext.createContext(params.getSessionId()));

    @Test
    public void storageAndPoolExists() {
        StorageDomain sd = new StorageDomain();
        Guid storagePoolId = Guid.newGuid();
        sd.setStoragePoolId(storagePoolId);
        when(storageDomainDao.getAllForStorageDomain(any())).thenReturn(Collections.singletonList(sd));
        when(storagePoolDao.get(storagePoolId)).thenReturn(new StoragePool());
        assertTrue(cmd.validate());
    }

    @Test
    public void invalidStoragePool() {
        StorageDomain sd = new StorageDomain();
        Guid storagePoolId = Guid.newGuid();
        sd.setStoragePoolId(storagePoolId);
        when(storageDomainDao.getAllForStorageDomain(any())).thenReturn(Collections.singletonList(sd));
        assertFalse(cmd.validate());
    }

    @Test
    public void invalidStorageDomain() {
        // No storage domain will be returned for no GUID therefore the validate will fail.
        assertFalse(cmd.validate());
    }
}
