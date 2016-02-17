package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * A test case for the {@link org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator} class.
 * The hasSpaceForClonedDisk() and hasSpaceForNewDisk() methods are covered separately in
 * {@link org.ovirt.engine.core.bll.validator.storage.StorageDomainValidatorFreeSpaceTest}.
 */
public class StorageDomainValidatorTest {
    private StorageDomain domain;
    private StorageDomainValidator validator;
    private static final int CRITICAL_SPACE_THRESHOLD = 5;

    @Before
    public void setUp() {
        domain = new StorageDomain();
        validator = new StorageDomainValidator(domain);
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotExists() {
        validator = new StorageDomainValidator(null);
        assertEquals("Wrong failure for null domain",
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST,
                validator.isDomainExistAndActive().getMessage());
    }

    @Test
    public void testIsDomainExistAndActiveDomainNotUp() {
        domain.setStatus(StorageDomainStatus.Inactive);
        assertEquals("Wrong failure for inactive domain",
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                validator.isDomainExistAndActive().getMessage());
    }

    @Test
    public void testIsDomainExistAndActiveDomainUp() {
        domain.setStatus(StorageDomainStatus.Active);
        assertTrue("domain should be up", validator.isDomainExistAndActive().isValid());
    }

    @Test
    public void testDomainWithNotEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(3, 756, StorageType.NFS));
        assertEquals("Wrong failure for not enough space",
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.isDomainWithinThresholds().getMessage());
    }

    @Test
    public void testDomainWithEnoughSpace() {
        validator = new StorageDomainValidator(mockStorageDomain(6, 756, StorageType.NFS));
        assertTrue("Domain should have more space then threshold", validator.isDomainWithinThresholds().isValid());
    }

    private static StorageDomain mockStorageDomain(int availableSize, int usedSize, StorageType storageType) {
        StorageDomain sd = new StorageDomain();
        sd.setAvailableDiskSize(availableSize);
        sd.setUsedDiskSize(usedSize);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStorageType(storageType);
        sd.setCriticalSpaceActionBlocker(CRITICAL_SPACE_THRESHOLD);
        return sd;
    }
}
