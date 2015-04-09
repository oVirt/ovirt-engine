package org.ovirt.engine.core.bll.memory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

public class MemoryStorageHandlerTest {

    public static final long META_DATA_SIZE_IN_GB = 1;
    public static final Integer LOW_SPACE_IN_GB = 3;
    public static final Integer ENOUGH_SPACE_IN_GB = 4;
    public static final Integer THRESHOLD_IN_GB = 4;
    public static final Integer THRESHOLD_HIGH_GB = 10;
    public static final int VM_SPACE_IN_MB = 2000;

    @Test
    public void verifyDomainForMemory() {
        Guid sdId = Guid.newGuid();
        List<StorageDomain> storageDomains = createStorageDomains(sdId);
        long vmSpaceInBytes = SizeConverter.convert(VM_SPACE_IN_MB,
                SizeConverter.SizeUnit.MiB,
                SizeConverter.SizeUnit.BYTES).intValue();
        List<DiskImage> disksList =  MemoryUtils.createDiskDummies(vmSpaceInBytes, META_DATA_SIZE_IN_GB);

        StorageDomain storageDomain = MemoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertThat(storageDomain, notNullValue());
        if (storageDomain != null) {
            Guid selectedId = storageDomain.getId();
            assertThat(selectedId.equals(sdId), is(true));
        }
        storageDomain.setCriticalSpaceActionBlocker(THRESHOLD_HIGH_GB);

        storageDomain = MemoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertThat(storageDomain, nullValue());
    }

    private static List<StorageDomain> createStorageDomains(Guid sdIdToBeSelected) {
        StorageDomain sd1 = createStorageDomain(Guid.newGuid(), StorageType.NFS, LOW_SPACE_IN_GB);
        StorageDomain sd2 = createStorageDomain(Guid.newGuid(), StorageType.NFS, LOW_SPACE_IN_GB);
        StorageDomain sd3 = createStorageDomain(sdIdToBeSelected, StorageType.NFS, ENOUGH_SPACE_IN_GB);
        List<StorageDomain> storageDomains = Arrays.asList(sd1, sd2, sd3);
        return storageDomains;
    }

    private static StorageDomain createStorageDomain(Guid guid, StorageType storageType, Integer size) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(guid);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        storageDomain.setStorageType(storageType);
        storageDomain.setStatus(StorageDomainStatus.Active);
        storageDomain.setAvailableDiskSize(size);
        storageDomain.setCriticalSpaceActionBlocker(THRESHOLD_IN_GB);
        return storageDomain;
    }
}
