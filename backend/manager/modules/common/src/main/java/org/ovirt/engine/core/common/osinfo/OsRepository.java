package org.ovirt.engine.core.common.osinfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.ConsoleTargetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.TpmSupport;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Version;

/**
 * Interface for accessing all Virtual OSs information.
 */
public interface OsRepository {

    int DEFAULT_X86_OS = 0;
    int DEFAULT_PPC_OS = 1001;
    int DEFAULT_S390_OS = 2001;

    /*
     * This value is used to enable the auto selection of an appropriate OS when
     * the user does not set a OS in the REST API.
     */
    int AUTO_SELECT_OS = -1;

    /**
     * @return all loaded os ids
     */
    List<Integer> getOsIds();

    /**
     * @return all unsupported os ids
     */
    Set<Integer> getUnsupportedOsIds();

    /**
     * Every configured OS a both a unique id and a name. The unique name
     * is not intended for presentation but for logic. The reason for having 2 different IDs
     * is for compatibility with the old numeric ID.
     * @return mapping of the osId to the unique OS name.
     *
     */
    Map<Integer, String> getUniqueOsNames();

    /**
     * @return map of osId to the os name
     */
    Map<Integer, String> getOsNames();

    String getOsName(int osId);

    /**
     * OS families are basically windows,linux and other.
     */
    String getOsFamily(int osId);

    /**
     * VM init type, cloudinit(linux), sysprep(windows) and ignition(coreos)
     */
    String getVmInitType(int osId);

    /**
     * @return a list of OSs who's {@link OsRepository#getOsFamily(int)} returns "linux"
     */
    List<Integer> getLinuxOss();

    /**
     * @return map of osId to the vmInitType
     */
    Map<Integer, String> getVmInitMap();


    List<Integer> get64bitOss();

    /**
     * @return a list of OSs who's {@link OsRepository#getOsFamily(int)} returns "windows"
     */
    List<Integer> getWindowsOss();

    /**
     * @return map of osId to the os architecture
     */
    Map<Integer, ArchitectureType> getOsArchitectures();

    /**
     * Get the architecture from OS
     * @param osId - OS id
     */
    ArchitectureType getArchitectureFromOS(int osId);

    /**
     * @return minimum RAM in MiB
     */
    int getMinimumRam(int osId, Version version);

    /**
     * @return maximum RAM in MiB
     */
    int getMaximumRam(int osId, Version version);

    /**
     * @return The supported graphics and display pairs for the given OS and cluster compatbility version
     */
    List<Pair<GraphicsType, DisplayType>> getGraphicsAndDisplays(int osId, Version version);

     /**
      * @return map (osId -> compatibility version -> list of (graphics, display) pairs) for all OSs and
      * compatibility versions
     */
    Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> getGraphicsAndDisplays();

     /**
      * @return Multiplier to apply in `vram' video RAM parameter computation.  0 to use default `vram' value.
     */
    int getVramMultiplier(int osId);

     /**
      * @return Multiplier to apply in `vgamem' video RAM parameter computation.  Defaults to 1 if unspecified.
     */
    int getVgamemMultiplier(int osId);

    /**
     * Checks if that OS network devices support hotplug.
     * @return an boolean
     */
    boolean hasNicHotplugSupport(int osId, Version version);

    /**
     * @return a map that contain an pair (OS id and version) with the hotplug support.
     */
    Map<Pair<Integer, Version>, Boolean> getNicHotplugSupportMap();

    /**
     * @return a map that contain an pair (OS id and version) with the disk hotpluggable interfaces.
     */
    Map<Pair<Integer, Version>, Set<String>> getDiskHotpluggableInterfacesMap();

    /**
     * this is Windows OSs specific path to the sysprep file
     */
    String getSysprepPath(int osId, Version version);

    /**
     * this is Windows OSs specific file name sysprep in the floppy,
     * ie: sysprep.inf for xp and 2003 and Unattend.xml for the new sysprep xml files
     */
    String getSysprepFileName(int osId, Version version);

    /**
     * this Windows OSs specific product key
     */
    String getProductKey(int osId, Version version);

    /**
     * a convenience method the for  family type "linux"
     */
    boolean isLinux(int osId);

    /**
     * a convenience method the for  family type "windows"
     */
    boolean isWindows(int osId);

    /**
     * a convenience method the for vmInit type "cloud init"
     */
    boolean isCloudInit(int osId);

    /**
     * a convenience method the for vmInit type "sys-prep"
     */
    boolean isSysprep(int osId);

    /**
     * a convenience method the for vmInit type "ignition"
     */
    boolean isIgnition(int osId);

    /**
     * @return list of supported disk interface devices
     */
    List<String> getDiskInterfaces(int osId, Version version, ChipsetType chipset);

    /**
     * @return list of supported network devices
     */
    List<String> getNetworkDevices(int osId, Version version);

    /**
     * @return set of disk hotpluggable interfaces
     */
    Set<String> getDiskHotpluggableInterfaces(int osId, Version version);

    /**
     * @return list of supported watch dog models
     */
    List<String> getWatchDogModels(int osId, Version version);

    /**
     * @return set of supported VmWatchdogTypes
     */
    Set<VmWatchdogType> getVmWatchdogTypes(int osId, Version version);

    /**
     * @return a specific sound device for the given OS and chipset.
     */
    String getSoundDevice(int osId, Version version, ChipsetType chipsetType);

    /**
     * @return the maximum allowed number of PCI devices
     */
    int getMaxPciDevices(int osId, Version version);

    /**
     * @param chipset the VM's chipset or null, if chipset is not defined
     * @return a specific CD interface for the given OS and chipset.
     */
    String getCdInterface(int osId, Version version, ChipsetType chipset);

    /**
     * @return if there is floppy support in the given os
     */
    boolean isFloppySupported(int osId, Version version);

    /**
     * early windows versions require a numeric identifier for sysprep to tell
     * the timezone. In later versions this was rectified and they use a universal name.
     */
    boolean isTimezoneValueInteger(int osId, Version version);

    /**
     * @return the os id. 0 if non found for that name.
     */
    int getOsIdByUniqueName(String uniqueOsName);

    /**
     * Get the default OS for given architecture
     */
    Map<ArchitectureType, Integer> getDefaultOSes();

    /**
     * Checks if is recommended enable the HyperV optimizations
     * @return an boolean
     */
    boolean isHypervEnabled(int osId, Version version);

    /**
     * Some Operating Systems don't support certain CPUs. As a result of working
     * with one,the guest OS might stop working, blue-screen, oops, or other well known red lights.
     * @return unsupported cpus mapping of {osId, version}->{set of cpu ids} ; cpu id is lower-case
     */
    Map<Pair<Integer, Version>, Set<String>> getUnsupportedCpus();

    /**
     * Stripped version of getUnsupportedCpus.
     */
    Set<String> getUnsupportedCpus(int osId, Version version);

    /**
     * Some Operating Systems don't support certain CPUs. As a result of working
     * with one,the guest OS might stop working, blue-screen, oops, or other well known red lights.
     * @param cpuId cpu id as being specified in vdc_options, <bold>case insensitive</bold>
     * @return true if the cpu supported otherwise false
     */
    boolean isCpuSupported(int osId, Version version, String cpuId);

    boolean isCpuHotplugSupported(int osId);

    boolean isCpuHotunplugSupported(int osId);

    /**
     * @return minimal number of CPUs required by OS
     */
    int getMinimumCpus(int osId);

    /**
     * @return a map that contain an pair (OS id and version) with the sound device support.
     */
    Map<Integer, Map<Version, Boolean>> getSoundDeviceSupportMap();

    /**
     * Checks if target OS architecture supports sound devices.
     */
    boolean isSoundDeviceEnabled(int osId, Version version);

    /**
     * @param osId operating system id
     * @param version may be null
     * @param chipset the VM's chipset or null, if chipset is not defined
     * @return USB controller for given operating system, cluster version and chipset; may be {@code null} if not specified
     */
    UsbControllerModel getOsUsbControllerModel(int osId, Version version, ChipsetType chipset);

    /**
     * @param osId operating system id
     * @param version may be null
     * @return console target type for given operating system and cluster version; may be {@code null} if not specified
     */
    ConsoleTargetType getOsConsoleTargetType(int osId, Version version);

    /**
     * Checks if the operating system requires special memory block when hot-plugging memory
     */
    boolean requiresHotPlugSpecialBlock(int osId, Version version);

    /**
     * Checks if the operation system requires legacy virtio support
     * @param osId operation system id
     * @param chipset the VM's chipset
     */
    boolean requiresLegacyVirtio(int osId, ChipsetType chipset);

    /**
     * Checks if the operation system requires TPM
     * @param osId operation system id
     */
    boolean requiresTpm(int osId);

    /**
     * Checks if the operation system requires a channel for ovirt guest agent support.
     * @param osId operation system id
     */
    boolean requiresOvirtGuestAgentChannel(int osId);

    /**
     * @return a map from OS id to TPM support flags
     */
    Map<Integer, TpmSupport> getTpmSupportMap();

    /**
     * Returns the operating system TPM support flag.
     * @param osId operation system id
     */
    TpmSupport getTpmSupport(int osId);

    /**
     * Checks if the operating system supports virtual TPM.
     * @param osId operation system id
     */
    boolean isTpmAllowed(int osId);

    /**
     * Checks if the operating system supports Q35 chipset
     * @param osId operation system id
     */
    boolean isQ35Supported(int osId);

    /**
     * Checks if the operating system supports SecureBoot
     * @param osId operation system id
     */
    boolean isSecureBootSupported(int osId);
}
