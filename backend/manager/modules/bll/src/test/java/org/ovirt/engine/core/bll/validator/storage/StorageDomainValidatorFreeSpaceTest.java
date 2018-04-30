package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainValidatorFreeSpaceTest {
    public static Stream<Arguments> createParams() {
        List<Arguments> params = new ArrayList<>();

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
                        disk.setStorageIds(Collections.singletonList(Guid.newGuid()));
                        disk.setSizeInGigabytes(200);
                        disk.setActualSize(100); // GB
                        disk.getSnapshots().add(DiskImage.copyOf(disk));

                        StorageDomain sd = new StorageDomain();
                        sd.setStorageType(storageType);
                        sd.setAvailableDiskSize(107); // GB

                        params.add(Arguments.of(disk, sd,
                                volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse,
                                volumeFormat == VolumeFormat.COW || volumeType == VolumeType.Sparse,
                                volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse
                        ));
                    }
                }
            }
        }

        return params.stream();
    }

    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateDiskWithSnapshots
            (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForSnapshots, sdValidator.hasSpaceForDiskWithSnapshots(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateClonedDisk
            (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForCloned, sdValidator.hasSpaceForClonedDisk(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateNewDisk
            (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForNew, sdValidator.hasSpaceForNewDisk(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateAllDisks(DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        String assertData = disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType();
        List<DiskImage> disksList = Collections.singletonList(disk);
        assertEquals(isValidForNew, sdValidator.hasSpaceForAllDisks(disksList, null).isValid(), assertData);
        assertEquals(isValidForCloned, sdValidator.hasSpaceForAllDisks(null, disksList).isValid(), assertData);
        assertEquals(isValidForNew && isValidForCloned, sdValidator.hasSpaceForAllDisks(disksList, disksList).isValid(),
                assertData);
    }
}
