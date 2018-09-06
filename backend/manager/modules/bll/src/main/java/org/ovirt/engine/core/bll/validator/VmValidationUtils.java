package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Version;

@Singleton
public class VmValidationUtils {
    @Inject
    private OsRepository osRepository;

    /**
     * Check if the memory size is within the correct limits (as per the configuration), taking into account the
     * OS type.
     *
     * @param osId The OS identifier.
     * @param memSizeInMB The memory size to validate.
     *
     * @return Is the memory within the configured limits or not.
     */
    public boolean isMemorySizeLegal(int osId, int memSizeInMB, Version clusterVersion) {
        return memSizeInMB >= getMinMemorySizeInMb(osId, clusterVersion) && memSizeInMB <= getMaxMemorySizeInMb(osId, clusterVersion);
    }

    /**
     * Check if the OS type is supported by the architecture type (as per the configuration).
     *
     * @param osId The OS identifier.
     * @param architectureType The architecture type to validate.
     *
     * @return If the OS type is supported.
     */
    public boolean isOsTypeSupported(int osId, ArchitectureType architectureType) {
        return architectureType == osRepository.getArchitectureFromOS(osId);
    }

    /**
     * Check if the OS type supports floppy devices
     *
     * @param osId The OS identifier.
     * @param clusterVersion The cluster version.
     *
     * @return If the floppy device is supported by the OS type.
     */
    public boolean isFloppySupported(int osId, Version clusterVersion) {
        return osRepository.isFloppySupported(osId, clusterVersion);
    }

    /**
     * Check if the OS type support the disk interface
     *
     * @param osId The OS identifier.
     * @param clusterVersion The cluster version.
     * @param diskInterface The disk interface.
     *
     * @return If the disk interface is supported by the OS type.
     */
    public boolean isDiskInterfaceSupportedByOs(
            int osId,
            Version clusterVersion,
            ChipsetType chipset,
            DiskInterface diskInterface) {
        List<String> diskInterfaces = osRepository.getDiskInterfaces(osId, clusterVersion, chipset);
        return diskInterfaces.contains(diskInterface.name());
    }

    /**
     * Check if the display type of the OS is supported (as per the configuration).
     *
     * @return a boolean
     */
    public boolean isGraphicsAndDisplaySupported(int osId, Version version, Collection<GraphicsType> graphics, DisplayType displayType) {
        for (GraphicsType graphicType : graphics) {
            if (!osRepository.getGraphicsAndDisplays(osId, version).contains(new Pair<>(graphicType, displayType))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the configured minimum VM memory size allowed.
     *
     * @return The minimum VM memory size allowed (as per configuration).
     */
    public Integer getMinMemorySizeInMb(int osId, Version version) {
        return osRepository.getMinimumRam(osId, version);
    }

    /**
     * Get the configured maximum VM memory size for this OS type.
     *
     * @param osId The type of OS to get the maximum memory for.
     *
     * @return The maximum VM memory setting for this OS (as per configuration).
     */
    public Integer getMaxMemorySizeInMb(int osId, Version clusterVersion) {
        return osRepository.getMaximumRam(osId, clusterVersion);
    }
}
