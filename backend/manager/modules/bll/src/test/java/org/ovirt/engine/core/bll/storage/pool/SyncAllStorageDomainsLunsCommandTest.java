package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

@ExtendWith(MockitoExtension.class)
public class SyncAllStorageDomainsLunsCommandTest {

    private Guid storagePoolId = Guid.newGuid();
    private SyncLunsParameters parameters = new SyncLunsParameters(storagePoolId);

    @InjectMocks
    private SyncAllStorageDomainsLunsCommand<SyncLunsParameters> command =
            new SyncAllStorageDomainsLunsCommand<>(parameters, null);

    @Mock
    private StorageDomainDao storageDomainDao;

    @Test
    public void getStorageDomainsToSync() {
        StorageDomain validSd = new StorageDomain();
        validSd.setId(Guid.newGuid());
        validSd.setStorageType(StorageType.ISCSI);
        validSd.setStatus(StorageDomainStatus.Active);

        StorageDomain nfsSd = new StorageDomain();
        nfsSd.setId(Guid.newGuid());
        nfsSd.setStorageType(StorageType.NFS);

        StorageDomain inactiveIscsiSd = new StorageDomain();
        inactiveIscsiSd.setId(Guid.newGuid());
        inactiveIscsiSd.setStorageType(StorageType.ISCSI);
        inactiveIscsiSd.setStatus(StorageDomainStatus.Inactive);

        List<StorageDomain> storageDomains = Arrays.asList(validSd, inactiveIscsiSd, nfsSd);
        when(storageDomainDao.getAllForStoragePool(storagePoolId)).thenReturn(storageDomains);
        assertEquals(Collections.singletonList(validSd),
                command.getStorageDomainsToSync().collect(Collectors.toList()));
    }
}
