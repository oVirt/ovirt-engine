package org.ovirt.engine.core.bll.memory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;

public class MemoryUtils {

    /** The size for the snapshot's meta data which is vm related properties at the
     *  time the snapshot was taken */
    public static final long META_DATA_SIZE_IN_BYTES = 10 * 1024;

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
        Set<String> memories = new HashSet<String>();
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
        memoryVolume.setDiskAlias("metadata");
        dataVolume.setvolumeFormat(VolumeFormat.COW);
        dataVolume.setVolumeType(VolumeType.Sparse);
        dataVolume.setSize(metadataSize);
        dataVolume.setActualSizeInBytes(metadataSize);
        dataVolume.getSnapshots().add(dataVolume);

        return Arrays.asList(memoryVolume, dataVolume);
    }
}
