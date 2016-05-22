package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;

@RunWith(Parameterized.class)
public class StorageDomainValidatorFreeSpaceTest {
    private DiskImage disk;
    private StorageDomain sd;

    private boolean isValidForCloned;
    private boolean isValidForNew;
    private boolean isValidForSnapshots;

    public StorageDomainValidatorFreeSpaceTest(DiskImage disk,
            StorageDomain sd,
            boolean isValidForCloned,
            boolean isValidForNew,
            boolean isValidForSnapshots) {
        this.disk = disk;
        this.sd = sd;
        this.isValidForCloned = isValidForCloned;
        this.isValidForNew = isValidForNew;
        this.isValidForSnapshots = isValidForSnapshots;
    }

    @Parameters
    public static Collection<Object[]> createParams() {
        List<Object[]> params = new ArrayList<>();

        for (StorageType storageType : StorageType.values()) {
            if (storageType.isConcreteStorageType() && !storageType.isCinderDomain()) {
                List<VolumeType> volumeTypes =
                        storageType.isFileDomain() ? Arrays.asList(VolumeType.Preallocated, VolumeType.Sparse)
                                : Collections.singletonList(VolumeType.Preallocated);
                for (VolumeType volumeType : volumeTypes) {
                    for (VolumeFormat volumeFormat : new VolumeFormat[] { VolumeFormat.COW, VolumeFormat.RAW }) {
                        DiskImage disk = new DiskImage();
                        disk.setVolumeFormat(volumeFormat);
                        disk.setVolumeType(volumeType);
                        disk.getSnapshots().add(disk);
                        disk.setSizeInGigabytes(200);
                        disk.setActualSize(100); // GB

                        StorageDomain sd = new StorageDomain();
                        sd.setStorageType(storageType);
                        sd.setAvailableDiskSize(107); // GB

                        params.add(new Object[] { disk, sd,
                                volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse,
                                volumeFormat == VolumeFormat.COW || volumeType == VolumeType.Sparse,
                                volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse
                        });
                    }
                }
            }
        }

        return params;
    }

    @Test
    public void testValidateDiskWithSnapshots() {
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType(),
                isValidForSnapshots,
                sdValidator.hasSpaceForDiskWithSnapshots(disk).isValid());
    }

    @Test
    public void testValidateClonedDisk() {
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType(),
                isValidForCloned,
                sdValidator.hasSpaceForClonedDisk(disk).isValid());
    }

    @Test
    public void testValidateNewDisk() {
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType(),
                isValidForNew,
                sdValidator.hasSpaceForNewDisk(disk).isValid());
    }

    @Test
    public void testValidateAllDisks() {
        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        String assertData = disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType();
        List<DiskImage> disksList = Collections.singletonList(disk);
        assertEquals(assertData,
                isValidForNew,
                sdValidator.hasSpaceForAllDisks(disksList, null).isValid());
        assertEquals(assertData,
                isValidForCloned,
                sdValidator.hasSpaceForAllDisks(null, disksList).isValid());
        assertEquals(assertData,
                isValidForNew && isValidForCloned,
                sdValidator.hasSpaceForAllDisks(disksList, disksList).isValid());
    }
}
