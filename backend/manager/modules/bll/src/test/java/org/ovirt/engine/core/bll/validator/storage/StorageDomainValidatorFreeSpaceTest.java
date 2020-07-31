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
                        disk.setActualSize(100); // GB

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
}
