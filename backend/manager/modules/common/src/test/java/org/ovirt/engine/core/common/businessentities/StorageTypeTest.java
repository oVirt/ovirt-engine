package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageTypeTest {

    @Test
    public void testIsFileDomain() {
        assertFalse(StorageType.FCP.isFileDomain());
        assertFalse(StorageType.ISCSI.isFileDomain());
        assertTrue(StorageType.NFS.isFileDomain());
        assertTrue(StorageType.LOCALFS.isFileDomain());
        assertTrue(StorageType.POSIXFS.isFileDomain());
        assertTrue(StorageType.GLUSTERFS.isFileDomain());
        assertTrue(StorageType.GLANCE.isFileDomain());
        assertFalse(StorageType.CINDER.isFileDomain());
        assertFalse(StorageType.MANAGED_BLOCK_STORAGE.isFileDomain());
    }

    @Test
    public void testIsBlockDomain() {
        assertTrue(StorageType.FCP.isBlockDomain());
        assertTrue(StorageType.ISCSI.isBlockDomain());
        assertFalse(StorageType.NFS.isBlockDomain());
        assertFalse(StorageType.LOCALFS.isBlockDomain());
        assertFalse(StorageType.POSIXFS.isBlockDomain());
        assertFalse(StorageType.GLUSTERFS.isBlockDomain());
        assertFalse(StorageType.GLANCE.isBlockDomain());
        assertFalse(StorageType.CINDER.isBlockDomain());
        assertFalse(StorageType.MANAGED_BLOCK_STORAGE.isBlockDomain());
    }

    @Test
    public void testNewStorageTypes() {
        assertEquals(11, StorageType.values().length,
                "A storage type was added/removed. Update this test, and the isFileDomain/isBlockDomain " +
                        "method accordingly");
    }
}
