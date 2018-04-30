package org.ovirt.engine.core.bll.memory.sdcomparators;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageTypeSharedFirstComparatorTest extends StorageDomainComparatorAbstractTest {

    public StorageTypeSharedFirstComparatorTest() {
        comparator = MemoryStorageHandler.SHARED_FIRST_COMPARATOR;
    }

    @Test
    public void testCompareLocalWithShared() {
        storageDomain1.setStorageType(StorageType.LOCALFS);
        storageDomain2.setStorageType(StorageType.NFS);
        assertSmallerThan(storageDomain2, storageDomain1);
    }

    @Test
    public void testCompareSharedWithLocal() {
        storageDomain1.setStorageType(StorageType.LOCALFS);
        storageDomain2.setStorageType(StorageType.NFS);
        assertBiggerThan(storageDomain1, storageDomain2);
    }

    @Test
    public void testCompareLocalWithLocal() {
        storageDomain1.setStorageType(StorageType.LOCALFS);
        storageDomain2.setStorageType(StorageType.LOCALFS);
        assertEqualsTo(storageDomain1, storageDomain2);
    }

    @Test
    public void testCompareSharedWithShared() {
        storageDomain1.setStorageType(StorageType.NFS);
        storageDomain2.setStorageType(StorageType.ISCSI);
        assertEqualsTo(storageDomain1, storageDomain2);
    }
}
