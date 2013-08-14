package org.ovirt.engine.core.common.osinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ovirt.engine.core.compat.Version;

/**
 * This class is holding all Virtual OSs information.
 */
public enum OsRepositoryImpl implements OsRepository {

    INSTANCE;

    private static final String OS_ROOT_NODE = "/os/";
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

    public void init(MapBackedPreferences preferences) {
        INSTANCE.preferences = preferences;
        emptyNode = preferences.node("emptyNode");
        buildIdToUnameLookup();
    }

    private void buildIdToUnameLookup() {
        try {
            String[] uniqueNames = preferences.node("/os").childrenNames();
            idToUnameLookup = new HashMap<Integer, String>(uniqueNames.length);
            for (String uniqueName : Arrays.asList(uniqueNames)) {
                Preferences idNode = getKeyNode(uniqueName, "id", null);
                if (idNode != null) {
                    idToUnameLookup.put(idNode.getInt("value", 0), uniqueName);
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException("Failed to initialize Os Repository due to " + e);
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
    public boolean isWindows(int osId) {
        return getOsFamily(osId).equalsIgnoreCase("windows");
    }

    @Override
    public ArrayList<String> getNetworkDevices(int osId, Version version) {
        String devices =
                getValueByVersion(idToUnameLookup.get(osId), "devices.network", version);
        return trimElements(devices.split(","));
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
    public boolean hasSpiceSupport(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "spiceSupport", version), false);
    }

    @Override
    public String getSysprepPath(int osId, Version version) {
        return getValueByVersion(idToUnameLookup.get(osId), "sysprepPath", version);
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
    public boolean isTimezoneValueInteger(int osId, Version version) {
        return getBoolean(getValueByVersion(idToUnameLookup.get(osId), "isTimezoneTypeInteger", version), false);
    }

    @Override
    public int getOsIdByUniqueName(String uniqueOsName) {
        for (Map.Entry<Integer, String> entry : getUniqueOsNames().entrySet()) {
            if (entry.getValue().equals(uniqueOsName)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private boolean getBoolean(String value, boolean defaultValue) {
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private int getInt(String value, int defaultValue) {
        return value == null ? defaultValue : Integer.parseInt(value);
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
     * @return new list where each value its whitespaces trimmed.
     */

    private ArrayList<String> trimElements(String... elements) {
        ArrayList<String> list = new ArrayList<String>(elements.length);
        for (String e : elements) {
            list.add(e.trim());
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
}
