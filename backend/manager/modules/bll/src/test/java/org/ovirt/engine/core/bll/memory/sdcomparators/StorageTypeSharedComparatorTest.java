package org.ovirt.engine.core.bll.memory.sdcomparators;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageTypeSharedComparatorTest extends StorageDomainComparatorAbstractTest {

    public StorageTypeSharedComparatorTest() {
        comparator = new StorageTypeSharedComparator();
    }

    @Test
    public void testCompareLocalWithShared() {
        storageDomain1.setStorageType(StorageType.NFS);
        storageDomain2.setStorageType(StorageType.LOCALFS);
        assertSmallerThan(storageDomain2, storageDomain1);
    }

    @Test
    public void testCompareSharedWithLocal() {
        storageDomain1.setStorageType(StorageType.NFS);
        storageDomain2.setStorageType(StorageType.LOCALFS);
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
