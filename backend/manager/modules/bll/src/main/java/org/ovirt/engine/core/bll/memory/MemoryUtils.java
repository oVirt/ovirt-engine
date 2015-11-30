package org.ovirt.engine.core.bll.memory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;

public class MemoryUtils {

    /** The size for the snapshot's meta data which is vm related properties at the
     *  time the snapshot was taken */
    public static final long METADATA_SIZE_IN_BYTES = 10 * 1024;

    private static final String VM_HIBERNATION_METADATA_DISK_DESCRIPTION = "meta-data for hibernated VM";
    private static final String VM_HIBERNATION_MEMORY_DISK_DESCRIPTION = "memory dump for hibernated VM";
    private static final String VM_HIBERNATION_METADATA_DISK_ALIAS_PATTERN = "%s_hibernation_metadata";
    private static final String VM_HIBERNATION_MEMORY_DISK_ALIAS_PATTERN = "%s_hibernation_memory";

    /**
     * Modified the given memory volume String representation to have the given storage
     * pool and storage domain
     */
    public static String changeStorageDomainAndPoolInMemoryState(
            String originalMemoryVolume, Guid storageDomainId, Guid storagePoolId) {
        List<Guid> guids = GuidUtils.getGuidListFromString(originalMemoryVolume);
        return createMemoryStateString(storageDomainId, storagePoolId,
                guids.get(2), guids.get(3), guids.get(4), guids.get(5));
    }

    public static String createMemoryStateString(
            Guid storageDomainId, Guid storagePoolId, Guid memoryImageId,
            Guid memoryVolumeId, Guid confImageId, Guid confVolumeId) {
        return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                storageDomainId,
                storagePoolId,
                memoryImageId,
                memoryVolumeId,
                confImageId,
                confVolumeId);
    }

    public static Set<String> getMemoryVolumesFromSnapshots(List<Snapshot> snapshots) {
        Set<String> memories = new HashSet<>();
        for (Snapshot snapshot : snapshots) {
            memories.add(snapshot.getMemoryVolume());
        }
        memories.remove(StringUtils.EMPTY);
        return memories;
    }

    public static List<DiskImage> createDiskDummies(long memorySize, long metadataSize) {
        DiskImage memoryVolume = new DiskImage();
        memoryVolume.setDiskAlias("memory");
        memoryVolume.setvolumeFormat(VolumeFormat.RAW);
        memoryVolume.setSize(memorySize);
        memoryVolume.setActualSizeInBytes(memorySize);
        memoryVolume.getSnapshots().add(memoryVolume);

        DiskImage dataVolume = new DiskImage();
        dataVolume.setDiskAlias("metadata");
        dataVolume.setvolumeFormat(VolumeFormat.RAW);
        dataVolume.setVolumeType(VolumeType.Preallocated);
        dataVolume.setSize(metadataSize);
        dataVolume.setActualSizeInBytes(metadataSize);
        dataVolume.getSnapshots().add(dataVolume);

        return Arrays.asList(memoryVolume, dataVolume);
    }

    public static DiskImage createHibernationMetadataDisk(VM vm) {
        DiskImage image = createMetadataDisk();
        image.setDiskAlias(generateHibernationMetadataDiskAlias(vm.getName()));
        image.setDescription(VM_HIBERNATION_METADATA_DISK_DESCRIPTION);
        return image;
    }

    public static DiskImage createMetadataDisk() {
        DiskImage image = new DiskImage();
        image.setSize(MemoryUtils.METADATA_SIZE_IN_BYTES);
        image.setVolumeType(VolumeType.Preallocated);
        image.setvolumeFormat(VolumeFormat.RAW);
        image.setDiskInterface(DiskInterface.VirtIO);
        return image;
    }

    private static String generateHibernationMetadataDiskAlias(String vmName) {
        return String.format(VM_HIBERNATION_METADATA_DISK_ALIAS_PATTERN, vmName);
    }

    public static DiskImage createHibernationMemoryDisk(VM vm, StorageType storageType) {
        DiskImage image = createMemoryDisk(vm, storageType);
        image.setDiskAlias(generateHibernationMemoryDiskAlias(vm.getName()));
        image.setDescription(VM_HIBERNATION_MEMORY_DISK_DESCRIPTION);
        return image;
    }

    public static DiskImage createMemoryDisk(VM vm, StorageType storageType) {
        DiskImage image = new DiskImage();
        image.setSize(vm.getTotalMemorySizeInBytes());
        image.setVolumeType(storageTypeToMemoryVolumeType(storageType));
        image.setvolumeFormat(VolumeFormat.RAW);
        image.setDiskInterface(DiskInterface.VirtIO);
        return image;
    }

    private static String generateHibernationMemoryDiskAlias(String vmName) {
        return String.format(VM_HIBERNATION_MEMORY_DISK_ALIAS_PATTERN, vmName);
    }

    /**
     * Returns whether to use Sparse or Preallocation. If the storage type is file system devices ,it would be more
     * efficient to use Sparse allocation. Otherwise for block devices we should use Preallocated for faster allocation.
     *
     * @return - VolumeType of allocation type to use.
     */
    public static VolumeType storageTypeToMemoryVolumeType(StorageType storageType) {
        return storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
    }
}
