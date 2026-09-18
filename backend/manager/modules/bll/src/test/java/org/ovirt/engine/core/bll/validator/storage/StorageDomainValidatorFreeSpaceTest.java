package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;


@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class StorageDomainValidatorFreeSpaceTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

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
                        if (volumeType == VolumeType.Preallocated) {
                            disk.setActualSize(200); // GB
                        } else {
                            disk.setActualSize(100); // GB
                        }

                        StorageDomain sd = new StorageDomain();
                        sd.setStorageType(storageType);
                        sd.setAvailableDiskSize(107); // GB

                        boolean shortCircuitForVendorManaged = storageType.isManagedBlockStorage();
                        boolean isValidForNew = shortCircuitForVendorManaged
                                || volumeType == VolumeType.Sparse;
                        boolean isValidForCloned = shortCircuitForVendorManaged
                                || volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse;
                        boolean isValidForSnapshots = shortCircuitForVendorManaged
                                || volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse;
                        params.add(Arguments.of(disk, sd,
                                isValidForCloned,
                                isValidForNew,
                                isValidForSnapshots
                        ));
                    }
                }
            }
        }

        return params.stream();
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateDiskWithSnapshots
    (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        disk.getSnapshots().add(DiskImage.copyOf(disk));

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForSnapshots, sdValidator.hasSpaceForDiskWithSnapshots(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateClonedDisk
    (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        disk.getSnapshots().add(DiskImage.copyOf(disk));

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForCloned, sdValidator.hasSpaceForClonedDisk(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateNewDisk
    (DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        disk.getSnapshots().add(DiskImage.copyOf(disk));

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(isValidForNew, sdValidator.hasSpaceForNewDisk(disk).isValid(),
                disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType());
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createParams")
    public void testValidateAllDisks(DiskImage disk, StorageDomain sd, boolean isValidForCloned, boolean isValidForNew, boolean isValidForSnapshots) {

        disk.getSnapshots().add(DiskImage.copyOf(disk));

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        String assertData = disk.getVolumeFormat() + ", " + disk.getVolumeType() + ", " + sd.getStorageType();
        List<DiskImage> disksList = Collections.singletonList(disk);
        assertEquals(isValidForNew, sdValidator.hasSpaceForAllDisks(disksList, null).isValid(), assertData);
        assertEquals(isValidForCloned, sdValidator.hasSpaceForAllDisks(null, disksList).isValid(), assertData);
        assertEquals(isValidForNew && isValidForCloned, sdValidator.hasSpaceForAllDisks(disksList, disksList).isValid(),
                assertData);
    }

    public static Stream<Arguments> createCowPreallocatedParams() {
        return Stream.of(
                // disk below threshold: valid
                Arguments.of(50, 107, StorageType.NFS, true),
                Arguments.of(50, 107, StorageType.ISCSI, true),
                // exact boundary (disk == available): valid
                Arguments.of(107, 107, StorageType.NFS, true),
                Arguments.of(107, 107, StorageType.ISCSI, true),
                // one GB over threshold: invalid
                Arguments.of(108, 107, StorageType.NFS, false),
                Arguments.of(108, 107, StorageType.ISCSI, false)
        );
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createCowPreallocatedParams")
    public void testValidateNewCowPreallocatedDisk
    (int diskSizeGb, int availableSizeGb, StorageType storageType, boolean expectedValid) {
        DiskImage disk = new DiskImage();
        disk.setVolumeFormat(VolumeFormat.COW);
        disk.setVolumeType(VolumeType.Preallocated);
        disk.setStorageIds(Collections.singletonList(Guid.newGuid()));
        disk.setSizeInGigabytes(diskSizeGb);

        StorageDomain sd = new StorageDomain();
        sd.setStorageType(storageType);
        sd.setAvailableDiskSize(availableSizeGb);

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(expectedValid, sdValidator.hasSpaceForNewDisk(disk).isValid(),
                "COW Preallocated: " + diskSizeGb + "GB disk, " + availableSizeGb + "GB available, " + storageType);
    }

    public static Stream<Arguments> createNewSparseVolumeParams() {
        final long gb = 1024L * 1024L * 1024L;
        return Stream.of(
                // block domain with initial size: vdsm allocates initialSize * 1.1 overhead
                Arguments.of(StorageType.ISCSI, 12, 10L * gb, true),
                Arguments.of(StorageType.ISCSI, 10, 10L * gb, false),
                // file domain without initial size: only the ~1MB qcow2 header is allocated
                Arguments.of(StorageType.NFS, 1, null, true),
                // block domain without initial size: full capacity is allocated
                Arguments.of(StorageType.ISCSI, 1, null, false),
                Arguments.of(StorageType.ISCSI, 200, null, true)
        );
    }

    @MockedConfig("mockConfiguration")
    @ParameterizedTest
    @MethodSource("createNewSparseVolumeParams")
    public void testValidateNewSparseVolume(StorageType storageType, int availableSizeGb, Long initialSizeBytes,
            boolean expectedValid) {
        DiskImage disk = new DiskImage();
        disk.setVolumeFormat(VolumeFormat.COW);
        disk.setVolumeType(VolumeType.Sparse);
        disk.setStorageIds(Collections.singletonList(Guid.newGuid()));
        disk.setSizeInGigabytes(200);

        StorageDomain sd = new StorageDomain();
        sd.setStorageType(storageType);
        sd.setAvailableDiskSize(availableSizeGb);

        StorageDomainValidator sdValidator = new StorageDomainValidator(sd);
        assertEquals(expectedValid, sdValidator.hasSpaceForNewSparseVolume(disk, initialSizeBytes).isValid(),
                "initialSize " + initialSizeBytes + ", " + availableSizeGb + "GB available, " + storageType);
    }
}
