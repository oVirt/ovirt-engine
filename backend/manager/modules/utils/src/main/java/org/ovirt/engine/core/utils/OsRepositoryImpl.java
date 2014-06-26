package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.MapBackedPreferences;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class is holding all Virtual OSs information.
 */
public enum OsRepositoryImpl implements OsRepository {

    INSTANCE;

    private static final Log log = LogFactory.getLog(OsRepositoryImpl.class);
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
    private static HashMap<ArchitectureType, Integer> defaultOsMap = new HashMap<ArchitectureType, Integer>(2);

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
            log.debugFormat("Osinfo Repository:\n {0}", toString());
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
                            log.warn("Illegal parent for os: " + uniqueName);
                        }
                    }
                }
        } catch (BackingStoreException e) {
            log.warn("Failed to validate Os Repository due to " + e);
            throw new RuntimeException("Failed to validate Os Repository due to " + e);
        }
    }

    private void buildIdToUnameLookup() {
        try {
            String[] uniqueNames = preferences.node("/os").childrenNames();
            idToUnameLookup = new HashMap<Integer, String>(uniqueNames.length);
            for (String uniqueName : uniqueNames) {
                Preferences idNode = getKeyNode(uniqueName, "id", null);
                if (idNode != null) {
                    idToUnameLookup.put(idNode.getInt("value", 0), uniqueName);
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException("Failed to initialize Os Repository due to " + e);
        }
    }

    private void buildBackCompatMapping() {
        try {
            String[] entries = preferences.node(BACKWARD_COMPATIBILITY_ROOT_NODE).keys();
            backwardCompatibleNamesToIds = new HashMap<String, Integer>(entries.length);
            for (String oldOsName : entries) {
                backwardCompatibleNamesToIds.put(oldOsName, preferences.node(BACKWARD_COMPATIBILITY_ROOT_NODE).getInt(oldOsName, 0));
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException("Failed to initialize Os Repository Backward Compatibility mappings due to " + e);
        }
    }

    @Override
    public ArrayList<Integer> getOsIds() {
        return new ArrayList<Integer>(idToUnameLookup.keySet());
    }

    @Override
    public HashMap<Integer, String> getUniqueOsNames() {
        // return a defensive copy to avoid modification of this map.
        return new HashMap<Integer, String>(idToUnameLookup);
    }

    @Override
    public HashMap<Integer, String> getOsNames() {
        HashMap<Integer, String> osNames = new HashMap<Integer, String>();
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

        List<Version> versions =
                new ArrayList<Version>(Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels));
        Map<Pair<Integer, Version>, Boolean> hotplugSupportOsIdVersionMap =
                new HashMap<Pair<Integer, Version>, Boolean>();

        for (Integer osId : getOsIds()) {
            for (Version version : versions) {
                hotplugSupportOsIdVersionMap.put(
                        new Pair<Integer, Version>(osId, version), hasNicHotplugSupport(osId, version));
            }
        }

        return hotplugSupportOsIdVersionMap;
    }

    @Override
    public Map<Pair<Integer, Version>, Set<String>> getDiskHotpluggableInterfacesMap() {
        Set<Version> versionsWithNull = new HashSet<Version>(Version.ALL);
        versionsWithNull.add(null);

        Map<Pair<Integer, Version>, Set<String>> diskHotpluggableInterfacesMap =
                new HashMap<Pair<Integer, Version>, Set<String>>();

        for (Integer osId : getOsIds()) {
            for (Version version : versionsWithNull) {
                diskHotpluggableInterfacesMap.put(
                        new Pair<Integer, Version>(osId, version), getDiskHotpluggableInterfaces(osId, version));
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
    public ArrayList<Integer> getLinuxOss() {
        ArrayList<Integer> oss = new ArrayList<Integer>();
        for (int osId : getOsIds()) {
            if (getOsFamily(osId).equalsIgnoreCase("linux")) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public ArrayList<Integer> get64bitOss() {
        ArrayList<Integer> oss = new ArrayList<Integer>();
        for (int osId : getOsIds()) {
            String bus = getValueByVersion(idToUnameLookup.get(osId), "bus", null);
            if ("64".equalsIgnoreCase(bus)) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public ArrayList<Integer> getWindowsOss() {
        ArrayList<Integer> oss = new ArrayList<Integer>();
        for (int osId : getOsIds()) {
            if (isWindows(osId)) {
                oss.add(osId);
            }
        }
        return oss;
    }

    @Override
    public HashMap<Integer, ArchitectureType> getOsArchitectures() {
        HashMap<Integer, ArchitectureType> osArchitectures = new HashMap<Integer, ArchitectureType>();
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
    public ArrayList<String> getDiskInterfaces(int osId, Version version) {
        String devices =
                getValueByVersion(idToUnameLookup.get(osId), "devices.diskInterfaces", version);
        return trimElements(devices.split(","));
    }

    @Override
    public ArrayList<String> getNetworkDevices(int osId, Version version) {
        String devices =
                getValueByVersion(idToUnameLookup.get(osId), "devices.network", version);
        return trimElements(devices.split(","));
    }

    @Override
    public Set<String> getDiskHotpluggableInterfaces(int osId, Version version) {
        String devices = getValueByVersion(idToUnameLookup.get(osId),
                "devices.disk.hotpluggableInterfaces",
                version);
        return new HashSet<String>(trimElements(devices.split(",")));
    }

    @Override
    public ArrayList<String> getWatchDogModels(int osId, Version version) {
        String models = getValueByVersion(idToUnameLookup.get(osId),
                "devices.watchdog.models",
                version);
        return trimElements(models.split(","));
    }

    @Override
    public Set<VmWatchdogType> getVmWatchdogTypes(int osId, Version version) {
        Set<VmWatchdogType> vmWatchdogTypes = new HashSet<VmWatchdogType>();

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
    public Map<Integer, Map<Version, List<DisplayType>>> getDisplayTypes() {
        Map<Integer, Map<Version, List<DisplayType>>> displayTypeMap = new HashMap<Integer, Map<Version, List<DisplayType>>>();
        Set<Version> versionsWithNull = new HashSet<Version>(Version.ALL);
        versionsWithNull.add(null);

        for (Integer osId : getOsIds()) {
            displayTypeMap.put(osId, new HashMap<Version, List<DisplayType>>());

            for (Version ver : versionsWithNull) {
                List<DisplayType> displayTypeList = getDisplayTypes(osId, ver);
                displayTypeMap.get(osId).put(ver, displayTypeList);
            }
        }

        return displayTypeMap;
    }

    public List<DisplayType> getDisplayTypes(int osId, Version version) {
        return new ArrayList<DisplayType>(parseDisplayProtocols(osId, version).keySet());
    }

    private Map<DisplayType, VmDeviceType> parseDisplayProtocols(int osId, Version version) {
        Map<DisplayType, VmDeviceType> parseDisplayProtocols = new LinkedHashMap<DisplayType, VmDeviceType>();

        String displayProtocolValue = getValueByVersion(idToUnameLookup.get(osId), "devices.display.protocols", version);
        for (String displayProtocol : displayProtocolValue.split(",")) {
            Pair<String, String> pairs = parseDisplayProtocol(displayProtocol);
            if (pairs != null) {
                parseDisplayProtocols.put(DisplayType.valueOf(pairs.getFirst()),
                        VmDeviceType.getByName(pairs.getSecond()));
            }
        }

        return parseDisplayProtocols;
    }

    private Pair<String, String> parseDisplayProtocol(String displayProtocol) {
        Pair<String, String> pairs = null;

        String[] displayProtocolSplit = displayProtocol.split("/");
        if (displayProtocolSplit.length == 2) {
            return new Pair<String, String>(displayProtocolSplit[0].trim(),
                    displayProtocolSplit[1].trim());
        }

        return pairs;
    }

    @Override
    public VmDeviceType getDisplayDevice(int osId, Version version, DisplayType displayType) {
        VmDeviceType vmDeviceType = parseDisplayProtocols(osId, version).get(displayType);
        return vmDeviceType == null ? displayType.getDefaultVmDeviceType() : vmDeviceType;
    }

    @Override
    public Map<Integer, Map<Version, Boolean>> getBalloonSupportMap() {
        Map<Integer, Map<Version, Boolean>> balloonSupportMap = new HashMap<Integer, Map<Version, Boolean>>();
        Set<Version> versionsWithNull = new HashSet<Version>(Version.ALL);
        versionsWithNull.add(null);

        Set<Integer> osIds = new HashSet<Integer>(getOsIds());
        for (Integer osId : osIds) {
            balloonSupportMap.put(osId, new HashMap<Version, Boolean>());

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
    public String getCdInterface(int osId, Version version) {
        return getValueByVersion(idToUnameLookup.get(osId), "devices.cdInterface", version);
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
     *
     * @param uniqueOsName
     * @param relativeKeyPath
     * @param version
     * @return
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
     * @param key
     * @return the node which its path is /os/$osId/path/to/key otherwise null
     * @throws BackingStoreException
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

    private ArrayList<String> trimElements(String... elements) {
        ArrayList<String> list = new ArrayList<String>(elements.length);
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
     *
     * @param version
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
            log.error(e.getStackTrace());
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
