package org.ovirt.engine.core.common.osinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Version;

/**
 * Interface for accessing all Virtual OSs information.
 */
public interface OsRepository {

    public final static int DEFAULT_X86_OS = 0;
    public final static int DEFAULT_PPC_OS = 1001;
    public final static int OLD_OTHER_ID = 6;

    /*
     * This value is used to enable the auto selection of an appropriate OS when
     * the user does not set a OS in the REST API.
     */
    public final static int AUTO_SELECT_OS = -1;

    /**
     * @return all loaded os ids
     */
    public ArrayList<Integer> getOsIds();

    /**
     * Every configured OS a both a unique id and a name. The unique name
     * is not intended for presentation but for logic. The reason for having 2 different IDs
     * is for compatibility with the old numeric ID.
     * @return mapping of the osId to the unique OS name.
     *
     */
    public HashMap<Integer, String> getUniqueOsNames();

    /**
     * @return map of osId to the the os name
     */
    public HashMap<Integer, String> getOsNames();

    public String getOsName(int osId);

    /**
     * OS families are basically windows,linux and other.
     * @param osId
     * @return
     */
    public String getOsFamily(int osId);

    /**
     * @return a list of OSs who's {@link OsRepository#getOsFamily(int)} returns "linux"
     */
    public ArrayList<Integer> getLinuxOss();

    public ArrayList<Integer> get64bitOss();

    /**
     * @return a list of OSs who's {@link OsRepository#getOsFamily(int)} returns "windows"
     */
    public ArrayList<Integer> getWindowsOss();

    /**
     * @return map of osId to the the os architecture
     */
    public HashMap<Integer, ArchitectureType> getOsArchitectures();

    /**
     * Get the architecture from OS
     * @param osId - OS id
     * @return
     */
    public ArchitectureType getArchitectureFromOS(int osId);

    /**
     * @return minimum RAM in mb
     */
    public int getMinimumRam(int osId, Version version);

    /**
     * @return maximum RAM in mb
     */
    public int getMaximumRam(int osId, Version version);

    /**
     * @return The supported display types for the given OS and cluster compatbility version
     */
    public List<DisplayType> getDisplayTypes(int osId, Version version);

     /**
      * @return map (osId -> compatibility version -> display types list) for all OSs and
      * compatibility versions
     */
    public Map<Integer, Map<Version, List<DisplayType>>> getDisplayTypes();

    /**
     * Get device type from display type of the OS
     * @param osId
     * @param version
     * @param displayType
     * @return
     */
    public VmDeviceType getDisplayDevice(int osId, Version version, DisplayType displayType);

    /**
     * @return map (osId -> compatibility version -> Boolean) that indicates balloon disabled for all OSs and
     * compatibility versions
     */
    public Map<Integer, Map<Version, Boolean>> getBalloonSupportMap();

    /**
     * Checks if is recommended enable the OS balloon.
     * @param osId
     * @param version
     * @return an boolean
     */
    public boolean isBalloonEnabled(int osId, Version version);

    /**
     * Checks if that OS network devices support hotplug.
     * @param osId
     * @param version
     * @return an boolean
     */
    public boolean hasNicHotplugSupport(int osId, Version version);

    /**
     * @return a map that contain an pair (OS id and version) with the hotplug support.
     */
    public Map<Pair<Integer, Version>, Boolean> getNicHotplugSupportMap();

    /**
     * @return a map that contain an pair (OS id and version) with the disk hotpluggable interfaces.
     */
    public Map<Pair<Integer, Version>, Set<String>> getDiskHotpluggableInterfacesMap();

    /**
     * this is Windows OSs specific path to the sysprep file
     * @param osId
     * @param version
     * @return
     */
    public String getSysprepPath(int osId, Version version);

    /**
     * this Windows OSs specific product key
     * @param osId
     * @param version
     * @return
     */
    public String getProductKey(int osId, Version version);

    /**
     * a convenience method the for  family type "linux"
     * @param osId
     * @return
     */
    public boolean isLinux(int osId);

    /**
     * a convenience method the for  family type "windows"
     * @param osId
     * @return
     */
    public boolean isWindows(int osId);

    /**
     * @param osId
     * @param version
     * @return list of supported disk interface devices
     */
    ArrayList<String> getDiskInterfaces(int osId, Version version);

    /**
     * @param osId
     * @return list of supported network devices
     */
    ArrayList<String> getNetworkDevices(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return set of disk hotpluggable interfaces
     */
    Set<String> getDiskHotpluggableInterfaces(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return list of supported watch dog models
     */
    ArrayList<String> getWatchDogModels(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return set of supported VmWatchdogTypes
     */
    Set<VmWatchdogType> getVmWatchdogTypes(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return a specific sound device for the given os.
     */
    String getSoundDevice(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return the maximum allowed number of PCI devices
     */
    public int getMaxPciDevices(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return a specific Cd Interface for the given os.
     */
    String getCdInterface(int osId, Version version);

    /**
     * early windows versions require a numeric identifier for sysprep to tell
     * the timezone. In later versions this was rectified and they use a universal name.
     * @param osId
     * @param version
     * @return
     */
    boolean isTimezoneValueInteger(int osId, Version version);

    /**
     * @param uniqueOsName
     * @return the os id. 0 if non found for that name.
     */
    int getOsIdByUniqueName(String uniqueOsName);

    /**
     * Get the default OS for given architecture
     * @return
     */
    Map<ArchitectureType, Integer> getDefaultOSes();

    /**
     * @param osId
     * @return
     */
    boolean isSingleQxlDeviceEnabled(int osId);

    /**
     * Checks if is recommended enable the HyperV optimizations
     * @param osId
     * @param version
     * @return an boolean
     */
    public boolean isHypervEnabled(int osId, Version version);

    /**
     * Some Operating Systems don't support certain CPUs. As a result of working
     * with one,the guest OS might stop working, blue-screen, oops, or other well known red lights.
     * @param osId
     * @param version
     * @return unsupported cpus mapping of {osId, version}->{set of cpu ids} ; cpu id is lower-case
     */
    public Map<Pair<Integer, Version>, Set<String>> getUnsupportedCpus();

    /**
     * Stripped version of getUnsupportedCpus.
     * @param osId
     * @param version
     * @return
     */
    public Set<String> getUnsupportedCpus(int osId, Version version);
    /**
     * Some Operating Systems don't support certain CPUs. As a result of working
     * with one,the guest OS might stop working, blue-screen, oops, or other well known red lights.
     * @param osId
     * @param version
     * @param cpuId cpu id as being specified in vdc_options, <bold>case insensitive</bold>
     * @return true if the cpu supported otherwise false
     */
    public boolean isCpuSupported(int osId, Version version, String cpuId);
}
