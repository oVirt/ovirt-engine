package org.ovirt.engine.core.common.businessentities;

import org.junit.Test;

import junit.framework.Assert;

public class StorageTypeTest {

    @Test
    public void testIsFileDomain() {
        Assert.assertFalse(StorageType.FCP.isFileDomain());
        Assert.assertFalse(StorageType.ISCSI.isFileDomain());
        Assert.assertTrue(StorageType.NFS.isFileDomain());
        Assert.assertTrue(StorageType.LOCALFS.isFileDomain());
        Assert.assertTrue(StorageType.POSIXFS.isFileDomain());
        Assert.assertTrue(StorageType.GLUSTERFS.isFileDomain());
    }

    @Test
    public void testIsBlockDomain() {
        Assert.assertTrue(StorageType.FCP.isBlockDomain());
        Assert.assertTrue(StorageType.ISCSI.isBlockDomain());
        Assert.assertFalse(StorageType.NFS.isBlockDomain());
        Assert.assertFalse(StorageType.LOCALFS.isBlockDomain());
        Assert.assertFalse(StorageType.POSIXFS.isBlockDomain());
        Assert.assertFalse(StorageType.GLUSTERFS.isBlockDomain());
    }

    @Test
    public void testNewStorageTypes() {
        Assert.assertTrue("A storage type was added/removed. Update this test, and the isFileDomain/isBlockDomain method accordingly", StorageType.values().length == 8);
    }
}
