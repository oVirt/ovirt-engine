package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;

import java.util.Arrays;

/**
 * A test case for the {@link StorageDomainValidator} class.
 * The hasSpaceForClonedDisk() and hasSpaceForNewDisk() methods are covered separately in
 * {@link StorageDomainValidatorFreeSpaceTest}.
 */
public class StorageDomainValidatorTest {
    private StorageDomain domain;
    private StorageDomainValidator validator;
    private final static int FREE_SPACE_CRITICAL_LOW_IN_GB = 5;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, FREE_SPACE_CRITICAL_LOW_IN_GB)
            );

    @Before
    public void setUp() {
        domain = new StorageDomain();
        validator = new StorageDomainValidator(domain);
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotExists() {
        validator = new StorageDomainValidator(null);
        assertEquals("Wrong failure for null domain",
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST,
                validator.isDomainExistAndActive().getMessage());
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotUp() {
        domain.setStatus(StorageDomainStatus.Inactive);
        assertEquals("Wrong failure for inactive domain",
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                validator.isDomainExistAndActive().getMessage());
    }

    @Test
    public void testIsDomainExistAndActiveDomainUp() {
        domain.setStatus(StorageDomainStatus.Active);
        assertTrue("domain should be up", validator.isDomainExistAndActive().isValid());
    }

    @Test
    public void testDomainWithNotEnoughSpaceForRequest() {
        validator = new StorageDomainValidator(mockStorageDomain(12, 748, StorageType.NFS));
        assertEquals("Wrong failure for not enough space for request",
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.isDomainHasSpaceForRequest(10).getMessage());
    }

    @Test
    public void testDomainWithNotEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(3, 756, StorageType.NFS));
        assertEquals("Wrong failure for not enough space",
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.isDomainWithinThresholds().getMessage());
    }

    @Test
    public void testDomainWithNotEnoughSpaceForRequestWithoutThreshold() {
        validator = new StorageDomainValidator(mockStorageDomain(12, 748, StorageType.NFS));
        assertEquals("Wrong failure for not enough space for request",
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.isDomainHasSpaceForRequest(13, false).getMessage());
    }

    @Test
    public void testDomainWithEnoughSpaceForRequestWithoutThreshold() {
        validator = new StorageDomainValidator(mockStorageDomain(12, 748, StorageType.NFS));
        assertTrue("Domain should have enough space for request.", validator.isDomainHasSpaceForRequest(12, false)
                .isValid());
    }

    @Test
    public void testDomainWithEnoughSpaceForRequest() {
        validator = new StorageDomainValidator(mockStorageDomain(16, 748, StorageType.NFS));
        assertTrue("Domain should have more space then threshold", validator.isDomainHasSpaceForRequest(10).isValid());
    }

    @Test
    public void testDomainWithEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(6, 756, StorageType.NFS));
        assertTrue("Domain should have more space then threshold", validator.isDomainWithinThresholds().isValid());
    }

    @Test
    public void testDomainWithEnoughSizeToRemoveDiskSnapshotsSumOfActualSizes() {
        validator = new StorageDomainValidator(mockStorageDomain(1024, 0, StorageType.NFS));
        DiskImage image1 = mockDiskImage(128, 1024);
        DiskImage image2 = mockDiskImage(256, 2048);

        assertTrue("Domain should have enough space for merging the snapshots",
            validator.hasSpaceForRemovingDiskSnapshots(Arrays.asList(image1, image2)).isValid());
    }

    @Test
    public void testDomainWithEnoughSizeToRemoveDiskSnapshotsMaxVirtualSize() {
        validator = new StorageDomainValidator(mockStorageDomain(1024, 0, StorageType.NFS));
        DiskImage image1 = mockDiskImage(1024, 1024);
        DiskImage image2 = mockDiskImage(256, 1024);

        assertTrue("Domain should have enough space for merging the snapshots",
            validator.hasSpaceForRemovingDiskSnapshots(Arrays.asList(image1, image2)).isValid());
    }

    @Test
    public void testDomainWithNotEnoughSizeToRemoveDiskSnapshotsSumOfActualSizes() {
        validator = new StorageDomainValidator(mockStorageDomain(1024, 0, StorageType.NFS));
        DiskImage image1 = mockDiskImage(1024, 1512);
        DiskImage image2 = mockDiskImage(256, 1512);

        assertTrue("Domain should not have enough space for merging the snapshots",
            !validator.hasSpaceForRemovingDiskSnapshots(Arrays.asList(image1, image2)).isValid());
    }

    @Test
    public void testDomainWithNotEnoughSizeToRemoveDiskSnapshotsMaxVirtualSize() {
        validator = new StorageDomainValidator(mockStorageDomain(512, 0, StorageType.NFS));
        DiskImage image1 = mockDiskImage(768, 1024);
        DiskImage image2 = mockDiskImage(768, 1024);

        assertTrue("Domain should not have enough space for merging the snapshots",
                !validator.hasSpaceForRemovingDiskSnapshots(Arrays.asList(image1, image2)).isValid());
    }

    private static StorageDomain mockStorageDomain(int availableSize, int usedSize, StorageType storageType) {
        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(availableSize);
        sd.setUsedDiskSize(usedSize);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStorageType(storageType);
        return sd;
    }

    private static DiskImage mockDiskImage(long actualSize, long virtualSize) {
        DiskImage image = new DiskImage();
        image.setActualSize(actualSize);
        image.setSize(virtualSize);

        return image;
    }
}
