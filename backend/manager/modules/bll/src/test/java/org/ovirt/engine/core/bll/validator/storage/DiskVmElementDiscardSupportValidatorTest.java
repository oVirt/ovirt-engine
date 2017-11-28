package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.di.InjectorRule;

@RunWith(Parameterized.class)
public class DiskVmElementDiscardSupportValidatorTest {

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Mock
    private StorageDomainDao storageDomainDao;

    private Disk disk;
    private static String diskAlias = "disk1";
    private Guid storageDomainId;
    private static String storageDomainName = "sd1";
    private DiskVmElementValidator validator;
    private ValidationResult expectedResult;

    public DiskVmElementDiscardSupportValidatorTest(Disk disk, boolean isPassDiscard, DiskInterface diskInterface,
            Long lunDiscardMaxSize, StorageType storageType, Boolean sdSupportsDiscard, Boolean diskWipeAfterDelete,
            ValidationResult expectedResult) {
        this.disk = disk;
        this.disk.setDiskAlias(diskAlias);
        DiskVmElement diskVmElement = new DiskVmElement();
        diskVmElement.setPassDiscard(isPassDiscard);
        diskVmElement.setDiskInterface(diskInterface);
        this.validator = new DiskVmElementValidator(this.disk, diskVmElement);

        if (lunDiscardMaxSize != null) {
            LUNs lun = new LUNs();
            lun.setDiscardMaxSize(lunDiscardMaxSize);
            ((LunDisk) this.disk).setLun(lun);
        }
        if (storageType != null) {
            initDiskStorageDomain(storageType, sdSupportsDiscard);
        }
        if (diskWipeAfterDelete != null) {
            this.disk.setWipeAfterDelete(diskWipeAfterDelete);
        }
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static Object[][] data() {
        ValidationResult passDiscardNotSupportedByDiskInterface = new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE,
                getDiskAliasVarReplacement());
        ValidationResult passDiscardNotSupportedForDirectLunByUnderlyingStorage = new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_FOR_DIRECT_LUN_BY_UNDERLYING_STORAGE,
                getDiskAliasVarReplacement());
        ValidationResult passDiscardNotSupportedForDiskImageByUnderlyingStorage = new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_FOR_DISK_IMAGE_BY_UNDERLYING_STORAGE,
                getDiskAliasVarReplacement(), getStorageDomainNameVarReplacement());
        ValidationResult passDiscardNotSupportedByCinder = new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_STORAGE_TYPE,
                getDiskAliasVarReplacement(), String.format("$diskStorageType %s", DiskStorageType.CINDER));

        // disk, isPassDiscard, diskInterface, lunDiscardMaxSize, storageType,
        // sdSupportsDiscard, diskWipeAfterDelete, expectedResult.
        return new Object[][] {
                // isPassDiscard == false
                {new DiskImage(), false, null, null, null,
                        null, null, ValidationResult.VALID},

                // Unsupported interface
                {new DiskImage(), true, DiskInterface.VirtIO, null, null,
                        null, null, passDiscardNotSupportedByDiskInterface},
                {new DiskImage(), true, DiskInterface.SPAPR_VSCSI, null, null,
                        null, null, passDiscardNotSupportedByDiskInterface},

                // Direct lun without support from underlying storage (different interfaces)
                {new LunDisk(), true, DiskInterface.VirtIO_SCSI, 0L, null,
                        null, null, passDiscardNotSupportedForDirectLunByUnderlyingStorage},
                {new LunDisk(), true, DiskInterface.IDE, 0L, null,
                        null, null, passDiscardNotSupportedForDirectLunByUnderlyingStorage},

                // Direct lun with support from underlying storage (different interfaces)
                {new LunDisk(), true, DiskInterface.VirtIO_SCSI, 1024L, null,
                        null, null, ValidationResult.VALID},
                {new LunDisk(), true, DiskInterface.IDE, 1024L, null,
                        null, null, ValidationResult.VALID},

                /*
                Image on file storage domain:
                - with/without support from underlying storage
                - different interfaces
                - different file storage types
                 */
                {new DiskImage(), true, DiskInterface.VirtIO_SCSI, null, StorageType.NFS,
                        true, null, ValidationResult.VALID},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.POSIXFS,
                        false, null, ValidationResult.VALID},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.POSIXFS,
                        null, null, ValidationResult.VALID},

                /*
                Image on block storage domain without support from underlying storage:
                - different interfaces
                - different block storage types
                 */
                {new DiskImage(), true, DiskInterface.VirtIO_SCSI, null, StorageType.ISCSI,
                        false, null, passDiscardNotSupportedForDiskImageByUnderlyingStorage},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.FCP,
                        false, null, passDiscardNotSupportedForDiskImageByUnderlyingStorage},

                /*
                Image on block storage domain with support from underlying storage:
                - different interfaces
                - different block storage types
                 */
                {new DiskImage(), true, DiskInterface.VirtIO_SCSI, null, StorageType.ISCSI,
                        true, null, ValidationResult.VALID},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.FCP,
                        true, null, ValidationResult.VALID},

                /*
                Image on block storage domain with support from underlying storage and WAD enabled:
                - different interfaces
                - different block storage types
                 */
                {new DiskImage(), true, DiskInterface.VirtIO_SCSI, null, StorageType.ISCSI,
                        true, true, new ValidationResult(EngineMessage
                        .ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED,
                        getStorageDomainNameVarReplacement(), getDiskAliasVarReplacement())},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.FCP,
                        true, true, new ValidationResult(EngineMessage
                        .ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED,
                        getStorageDomainNameVarReplacement(), getDiskAliasVarReplacement())},

                /*
                Image on non file or block storage domain:
                - different interfaces
                - different non file or block storage types
                 */
                {new DiskImage(), true, DiskInterface.VirtIO_SCSI, null, StorageType.UNKNOWN,
                        null, null, createPassDiscardNotSupportedByStorageTypeValResult(StorageType.UNKNOWN)},
                {new DiskImage(), true, DiskInterface.IDE, null, StorageType.CINDER,
                        null, null, createPassDiscardNotSupportedByStorageTypeValResult(StorageType.CINDER)},

                // Unsupported disk storage type (different interfaces)
                {new CinderDisk(), true, DiskInterface.VirtIO_SCSI, null, null,
                        null, null, passDiscardNotSupportedByCinder},
                {new CinderDisk(), true, DiskInterface.IDE, null, null,
                        null, null, passDiscardNotSupportedByCinder},
        };
    }

    @Test
    public void testPassDiscardSupport() {
        assertEquals(expectedResult, validator.isPassDiscardSupported(storageDomainId));
    }

    private void initDiskStorageDomain(StorageType storageType, Boolean sdSupportsDiscard) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStorageName(storageDomainName);
        storageDomain.setStorageType(storageType);
        storageDomain.setSupportsDiscard(sdSupportsDiscard);

        storageDomainId = Guid.newGuid();
        storageDomain.setId(storageDomainId);

        initMocks(this);
        injectorRule.bind(StorageDomainDao.class, storageDomainDao);
        when(storageDomainDao.get(storageDomainId)).thenReturn(storageDomain);
    }

    private static String getDiskAliasVarReplacement() {
        return String.format("$diskAlias %s", diskAlias);
    }

    private static String getStorageDomainNameVarReplacement() {
        return String.format("$storageDomainName %s", storageDomainName);
    }

    private static ValidationResult createPassDiscardNotSupportedByStorageTypeValResult(
            StorageType storageType) {
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_STORAGE_TYPE,
                getDiskAliasVarReplacement(), getStorageDomainNameVarReplacement(),
                String.format("$storageType %s", storageType));
    }
}
