package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.MapBackedPreferences;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is holding all Virtual OSs information.
 */
public enum OsRepositoryImpl implements OsRepository {

    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(OsRepositoryImpl.class);
    private static final String OS_ROOT_NODE = "/os/";
    private static final String BACKWARD_COMPATIBILITY_ROOT_NODE = "/backwardCompatibility";
    /**
     * the configuration tree holding all the os data.
     */
    private MapBackedPreferences preferences;
    private Preferences emptyNode;
    /**
     * lookup table to get the os id from the uniquename and vise-versa os.rhel6.id.value = 8 means its id in the engine
     * db is 8 and the unique name is "rhel6"
     */
    private Map<Integer, String> idToUnameLookup;
    private Map<String, Integer> backwardCompatibleNamesToIds;
    private static Map<ArchitectureType, Integer> defaultOsMap = new HashMap<>(2);

    static {
        defaultOsMap.put(ArchitectureType.x86_64, DEFAULT_X86_OS);
        defaultOsMap.put(ArchitectureType.ppc64, DEFAULT_PPC_OS);
    }

    public void init(MapBackedPreferences preferences) {
        INSTANCE.preferences = preferences;
        emptyNode = preferences.node("emptyNode");
        buildIdToUnameLookup();
        buildBackCompatMapping();
        validateTree();
        if (log.isDebugEnabled()) {
            log.debug("Osinfo Repository:\n{}", this);
        }
    }

    private void validateTree() {
        try {
                String[] uniqueNames = preferences.node("/os").childrenNames();
                for (String uniqueName : Arrays.asList(uniqueNames)) {
                    Preferences node = getKeyNode(uniqueName, "derivedFrom", null);
                    String id = getKeyNode(uniqueName, "id", null).get("value", "0");
                    if (node != null) {
                        String derivedFrom = node.get("value", null);
                        if (derivedFrom != null && !idToUnameLookup.containsValue(derivedFrom)) {
                            idToUnameLookup.remove(Integer.valueOf(id));
                            preferences.node("/os/" + uniqueName).removeNode();
                            log.warn("Illegal parent for os '{}'", uniqueName);
                        }
                    }
                }
        } catch (BackingStoreException e) {
            log.warn("Failed to validate Os Repository due to {}", e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException("Failed to validate Os Repository due to " + e);
        }
    }

    private void buildIdToUnameLookup() {
        try {
            String[] uniqueNames = preferences.node("/os").childrenNames();
            idToUnameLookup = new HashMap<>(uniqueNames.length);
            for (String uniqueName : uniqueNames) {
                Preferences idNode = getKeyNode(uniqueName, "id", null);
                if (idNode != null) {
                    int osId = idNode.getInt("value", 0);
                    if (idNode != emptyNode && idToUnameLookup.containsKey(osId)) {
                        throw new RuntimeException(String.format("colliding os id %s at node %s", osId, idNode.absolutePath()));
                    } else {
                        idToUnameLookup.put(osId, uniqueName);
                    }
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException("Failed to initialize Os Repository due to " + e);
        }
    }

    private void buildBackCompatMapping() {
        try {
            String[] entries = preferences.node(BACKWARD_COMPATIBILITY_ROOT_NODE).keys();
            backwardCompatibleNamesToIds = new HashMap<>(entries.length);
            for (String oldOsName : entries) {
                backwardCompatibleNamesToIds.put(oldOsName, preferences.node(BACKWARD_COMPATIBILITY_ROOT_NODE).getInt(oldOsName, 0));
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException("Failed to initialize Os Repository Backward Compatibility mappings due to " + e);
        }
    }

    @Override
    public List<Integer> getOsIds() {
        return new ArrayList<>(idToUnameLookup.keySet());
    }

    @Override
    public Map<Integer, String> getUniqueOsNames() {
        // return a defensive copy to avoid modification of this map.
        return new HashMap<>(idToUnameLookup);
    }

    @Override
    public Map<Integer, String> getOsNames() {
        Map<Integer, String> osNames = new HashMap<>();
        for (int osId : getOsIds()) {
            String name = getValueByVersion(idToUnameLookup.get(osId), "name", null);
            if (name != null) {
                osNames.put(osId, name);
            }
        }
        return osNames;
    }

    @Override
    public Map<Pair<Integer, Version>, Boolean> getNicHotplugSupportMap() {

        List<Version> versions = new ArrayList<>(Config.<HashSet<Version>>getValue(ConfigValues.SupportedClusterLevels));
        Map<Pair<Integer, Version>, Boolean> hotplugSupportOsIdVersionMap = new HashMap<>();

        for (Integer osId : getOsIds()) {
            for (Version version : versions) {
                hotplugSupportOsIdVersionMap.put(
                        new Pair<>(osId, version), hasNicHotplugSupport(osId, version));
            }
        }

        return hotplugSupportOsIdVersionMap;
    }

    @Override
    public Map<Pair<Integer, Version>, Set<String>> getDiskHotpluggableInterfacesMap() {
        Set<Version> versionsWithNull = new HashSet<>(Version.ALL);
        versionsWithNull.add(null);

        Map<Pair<Integer, Version>, Set<String>> diskHotpluggableInterfacesMap = new HashMap<>();

        for (Integer osId : getOsIds()) {
            for (Version version : versionsWithNull) {
                diskHotpluggableInterfacesMap.put(
                        new Pair<>(osId, version), getDiskHotpluggableInterfaces(osId, version));
            }
        }

        return diskHotpluggableInterfacesMap;
    }

    @Override
    public String getOsName(int osId) {
        return getOsNames().get(osId);
    }

    @Override
    public String getOsFamily(int osId) {
        return getValueByVersion(idToUnameLookup.get(osId), "family", null);
    }

    @Override
    public List<Integer> getLinuxOss() {
        List<Integer> oss = new ArrayList<>();
        for (int osId : getOsIds()) {
            if (getOsFamily(osId).equalsIgnoreCase("linux")) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public List<Integer> get64bitOss() {
        List<Integer> oss = new ArrayList<>();
        for (int osId : getOsIds()) {
            String bus = getValueByVersion(idToUnameLookup.get(osId), "bus", null);
            if ("64".equalsIgnoreCase(bus)) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public List<Integer> getWindowsOss() {
        List<Integer> oss = new ArrayList<>();
        for (int osId : getOsIds()) {
            if (isWindows(osId)) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public Map<Integer, ArchitectureType> getOsArchitectures() {
        Map<Integer, ArchitectureType> osArchitectures = new HashMap<>();
        for (int osId : getOsIds()) {
            String architecture = getValueByVersion(idToUnameLookup.get(osId), "cpuArchitecture", null);

            if (architecture != null) {
                osArchitectures.put(osId, ArchitectureType.valueOf(architecture));
            }
        }
        return osArchitectures;
    }

    @Override
    public ArchitectureType getArchitectureFromOS(int osId) {
        String architecture = getValueByVersion(idToUnameLookup.get(osId), "cpuArchitecture", null);
        return ArchitectureType.valueOf(architecture);
    }

    @Override
    public boolean isWindows(int osId) {
        return getOsFamily(osId).equalsIgnoreCase("windows");
    }

    @Override
    public List<String> getDiskInterfaces(int osId, Version version) {
        String devices =
                getValueByVersion(idToUnameLookup.get(osId), "devices.diskInterfaces", version);
        return trimElements(devices.split(","));
    }

    @Override
    public List<String> getNetworkDevices(int osId, Version version) {
        String devices =
                getValueByVersion(idToUnameLookup.get(osId), "devices.network", version);
        return trimElements(devices.split(","));
    }

    @Override
    public Set<String> getDiskHotpluggableInterfaces(int osId, Version version) {
        String devices = getValueByVersion(idToUnameLookup.get(osId),
                "devices.disk.hotpluggableInterfaces",
                version);
        return new HashSet<>(trimElements(devices.split(",")));
    }

    @Override
    public List<String> getWatchDogModels(int osId, Version version) {
        String models = getValueByVersion(idToUnameLookup.get(osId),
                "devices.watchdog.models",
                version);
        return trimElements(models.split(","));
    }

    @Override
    public Set<VmWatchdogType> getVmWatchdogTypes(int osId, Version version) {
        Set<VmWatchdogType> vmWatchdogTypes = new HashSet<>();

        for (String watchDogModel : getWatchDogModels(osId, version)) {
            vmWatchdogTypes.add(VmWatchdogType.getByName(watchDogModel));
        }

        return vmWatchdogTypes;
    }

    @Override
    public boolean isLinux(int osId) {
        return getOsFamily(osId).equalsIgnoreCase("linux");
    }

    @Override
    public int getMinimumRam(int osId, Version version) {
        return getInt(getValueByVersion(idToUnameLookup.get(osId), "resources.minimum.ram", version), -1);
    }

    @Override
    public int getMaximumRam(int osId, Version version) {
        return getInt(getValueByVersion(idToUnameLookup.get(osId), "resources.maximum.ram", version), -1);
    }

    @Override
    public Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> getGraphicsAndDisplays() {
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> supportedGraphicsAndDisplaysMap = new HashMap<>();

        Set<Version> versionsWithNull = new HashSet<>(Version.ALL);
        versionsWithNull.add(null);

        for (Integer osId : getOsIds()) {
            supportedGraphicsAndDisplaysMap.put(osId, new HashMap<>());

            for (Version ver : versionsWithNull) {
                List<Pair<GraphicsType, DisplayType>> displayTypeList = getGraphicsAndDisplays(osId, ver);
                supportedGraphicsAndDisplaysMap.get(osId).put(ver, displayTypeList);
            }
        }

        return supportedGraphicsAndDisplaysMap;
    }

    public List<Pair<GraphicsType, DisplayType>> getGraphicsAndDisplays(int osId, Version version) {
        return parseDisplayProtocols(osId, version);
    }

    private List<Pair<GraphicsType, DisplayType>> parseDisplayProtocols(int osId, Version version) {
        List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays = new ArrayList<>();

        String displayAndGraphicsLine = getValueByVersion(idToUnameLookup.get(osId), "devices.display.protocols", version); // todo - use different key?
        for (String displayAndGraphics : displayAndGraphicsLine.split(",")) {
            Pair<String, String> pair = parseSlashSeparatedPair(displayAndGraphics);
            if (pair != null) {
                GraphicsType graphics = GraphicsType.fromString(pair.getFirst());
                DisplayType display = DisplayType.valueOf(pair.getSecond());

                graphicsAndDisplays.add(new Pair<>(graphics, display));
            }
        }

        return graphicsAndDisplays;
    }

    private static Pair<String, String> parseSlashSeparatedPair(String slashSeparatedString) {
        List<String> splitted = trimElements(slashSeparatedString.split("/"));

        return (splitted.size() == 2)
            ? new Pair<>(splitted.get(0), splitted.get(1))
            : null;
    }

    @Override
    public int getVramMultiplier(int osId) {
        return getInt(getValueByVersion(idToUnameLookup.get(osId), "devices.display.vramMultiplier", null), 0);
    }

    @Override
    public Map<Integer, Map<Version, Boolean>> getBalloonSupportMap() {
        Map<Integer, Map<Version, Boolean>> balloonSupportMap = new HashMap<>();
        Set<Version> versionsWithNull = new HashSet<>(Version.ALL);
        versionsWithNull.add(null);

        Set<Integer> osIds = new HashSet<>(getOsIds());
        for (Integer osId : osIds) {
            balloonSupportMap.put(osId, new HashMap<>());

            for (Version ver : versionsWithNull) {
                balloonSupportMap.get(osId).put(ver, isBalloonEnabled(osId, ver));
            }
        }

        return balloonSupportMap;
    }

    @Override
    public boolean isBalloonEnabled(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "devices.balloon.enabled", version), false);
    }

    @Override
    public boolean hasNicHotplugSupport(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "devices.network.hotplugSupport", version), false);
    }

    @Override
    public String getSysprepPath(int osId, Version version) {
        return EngineLocalConfig.getInstance().expandString(getValueByVersion(idToUnameLookup.get(osId), "sysprepPath", version));
    }

    @Override
    public String getSysprepFileName(int osId, Version version) {
        return getValueByVersion(idToUnameLookup.get(osId), "sysprepFileName", version);
    }

    @Override
    public String getProductKey(int osId, Version version) {
        return getValueByVersion(idToUnameLookup.get(osId), "productKey", version);
    }

    @Override
    public String getSoundDevice(int osId, Version version) {
        return getValueByVersion(idToUnameLookup.get(osId), "devices.audio", version);
    }

    @Override
    public int getMaxPciDevices(int osId, Version version) {
        return getInt(getValueByVersion(idToUnameLookup.get(osId), "devices.maxPciDevices", version), -1);
    }

    @Override
    public String getCdInterface(int osId, Version version, ChipsetType chipset) {
        String line = getValueByVersion(idToUnameLookup.get(osId), "devices.cdInterface", version);
        String defaultInterface = null;
        for (String element : line.split(",")) {
            Pair<String, String> pair = parseSlashSeparatedPair(element);
            if (pair == null) {
                defaultInterface = element.trim().toLowerCase();
            } else if (chipset != null && chipset.getChipsetName().equalsIgnoreCase(pair.getFirst())) {
                return pair.getSecond().toLowerCase();
            }
        }
        return defaultInterface;
    }

    @Override
    public boolean isFloppySupported(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "devices.floppy.support", version), false);
    }

    @Override
    public boolean isTimezoneValueInteger(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "isTimezoneTypeInteger", version), false);
    }

    @Override
    public boolean isHypervEnabled(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "devices.hyperv.enabled", version), false);
    }

    @Override
    public Map<Pair<Integer, Version>, Set<String>> getUnsupportedCpus() {
        Set<Version> versionsWithNull = new HashSet<>(Version.ALL);
        versionsWithNull.add(null);

        Map<Pair<Integer, Version>, Set<String>> unsupportedCpus = new HashMap<>();

        for (int osId : getOsIds()) {
            for (Version version : versionsWithNull) {
                unsupportedCpus.put(
                        new Pair<>(osId, version),
                        getUnsupportedCpus(osId, version)
                );
            }
        }
        return unsupportedCpus;
    }

    @Override
    public boolean isCpuSupported(int osId, Version version, String cpuId) {
        return !getUnsupportedCpus(osId, version).contains(cpuId.toLowerCase());
    }

    @Override
    public Map<Integer, Map<Version, Boolean>> getSoundDeviceSupportMap() {
        Map<Integer, Map<Version, Boolean>> soundDeviceSupportMap = new HashMap<>();
        Set<Version> versionsWithNull = new HashSet<>(Version.ALL);
        versionsWithNull.add(null);

        for (Integer osId : getOsIds()) {
            soundDeviceSupportMap.put(osId, new HashMap<>());

            for (Version ver : versionsWithNull) {
                soundDeviceSupportMap.get(osId).put(ver, isSoundDeviceEnabled(osId, ver));
            }
        }

        return soundDeviceSupportMap;
    }

    @Override
    public boolean isSoundDeviceEnabled(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "devices.audio.enabled", version), false);
    }

    @Override
    public int getOsIdByUniqueName(String uniqueOsName) {
        for (Map.Entry<Integer, String> entry : getUniqueOsNames().entrySet()) {
            if (entry.getValue().equals(uniqueOsName)) {
                return entry.getKey();
            }
        }

        if (getBackwardCompatibleNamesToIds().containsKey(uniqueOsName)) {
            return getBackwardCompatibleNamesToIds().get(uniqueOsName);
        }

        return 0;
    }

    @Override
    public Set<String> getUnsupportedCpus(int osId, Version version) {
        return new HashSet<>(trimElements(
                getValueByVersion(
                        idToUnameLookup.get(osId),
                        "cpu.unsupported",
                        version)
                        .toLowerCase().split(",")));
    }

    private boolean getBoolean(String value, boolean defaultValue) {
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private int getInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * get the value of the key specified by its version or the default version if not exist. see
     * {@link OsRepositoryImpl#getKeyNode}
     */
    private String getValueByVersion(String uniqueOsName, String relativeKeyPath, Version version) {
        Preferences keyNode = getKeyNode(uniqueOsName, relativeKeyPath, version);
        if (keyNode == emptyNode) {
            version = null;
            keyNode = getKeyNode(uniqueOsName, relativeKeyPath, null);
        }
        return keyNode.get(versionedValuePath(version), "");
    }

    /**
     *
     * @param uniqueOsName
     *            is the os.{String} section of the key path. \e.g "rhel6" is the unique os name of os.rhel6.description
     *            key
     * @param version
     *            the value version. e.g ver/os/rhel6/devices/sound/version.3.3 = ac97
     * @return the node of the specified key for the given osId or its derived parent. Essentially this method will
     *         recursively be called till no parent with the exact path is found.
     *
     */
    private Preferences getKeyNode(String uniqueOsName, String relativeKeyPath, Version version) {
        if (uniqueOsName == null) {
            return emptyNode;
        }
        // first try direct OS node
        try {
            Preferences node = getNodeIfExist(uniqueOsName, relativeKeyPath);
            if (node != null && Arrays.asList(node.keys()).contains(versionedValuePath(version))) {
                return node;
            } else {
                // if not exist directly on the OS consult the one its derived from
                String derivedFromOs = preferences.node(OS_ROOT_NODE + uniqueOsName + "/derivedFrom").get("value", null);
                return derivedFromOs == null ? emptyNode : getKeyNode(derivedFromOs, relativeKeyPath, version);
            }
        } catch (BackingStoreException e) {
            // our preferences impl should use storage to back the data structure
            // throwing unchecked exception here to make sure this anomality is noticed
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param osId unique name identifier. this is NOT the "id" attribute which is kept for backward compatibility.
     * @return the node which its path is /os/$osId/path/to/key otherwise null
     */
    private Preferences getNodeIfExist(String osId, String key) throws BackingStoreException {
        // make a full path name of some.key to os/$osId/some/key
        String pathName = OS_ROOT_NODE + osId + "/" + key.replaceAll("\\.", "/");
        if (preferences.nodeExists(pathName)) {
            return preferences.node(pathName);
        }
        return null;
    }

    /**
     * helper method to retrieve a list of trimmed elements<br>
     * <p>
     * <code><b> foo, bar ,   baz </b></code> results <code><b>foo,bar,baz</b></code>
     * </p>
     *
     * @param elements
     *            vararg of string elements.
     * @return new list where each value its whitespaces trimmed, and
     * is not added empty values.
     */

    private static List<String> trimElements(String... elements) {
        List<String> list = new ArrayList<>(elements.length);
        for (String e : elements) {
            e = e.trim();
            if (e.length() > 0) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * a key can have several values per version. a null version represents the default while other are specific one:
     * key.value = someval // the default value. the path returned is "value" key.value.3.1 = otherval // the 3.1
     * version val. the path returned is "value.3.1"
     *
     * @return the string representation of the value path. for key.value.3.1 = otherval "value.3.1" should be returned.
     */
    private String versionedValuePath(Version version) {
        return version == null ? "value" : "value." + version.toString();
    }

    @Override
    public boolean isSingleQxlDeviceEnabled(int osId) {
        return isLinux(osId);
    }

    public Map<String, Integer> getBackwardCompatibleNamesToIds() {
        return backwardCompatibleNamesToIds;
    }

    @Override
    public Map<ArchitectureType, Integer> getDefaultOSes() {
        return defaultOsMap;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            walkTree(sb, preferences);
        } catch (BackingStoreException e) {
            log.error("Error traversing OS tree: {}", e.getMessage());
            log.debug("Exception", e);
        }
        return sb.toString();
    }

    private void walkTree(StringBuilder sb, Preferences node) throws BackingStoreException {
        if (node.childrenNames().length == 0) {
            sb.append(
                    node.absolutePath()
                            .replaceFirst("/", "")
                            .replace("/", "."));
            for (String k : node.keys()) {
                sb.append("\n\t")
                        .append(k)
                        .append("=")
                        .append(node.get(k, ""));
            }
            sb.append("\n");
        } else {
            for (String nodePath : node.childrenNames()) {
                walkTree(sb, node.node(nodePath));
            }
        }

    }
}
