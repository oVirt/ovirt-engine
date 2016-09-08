package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
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
    }

    @Test
    public void testNewStorageTypes() {
        assertEquals("A storage type was added/removed. Update this test, and the isFileDomain/isBlockDomain " +
                "method accordingly", 9, StorageType.values().length);
    }
}
