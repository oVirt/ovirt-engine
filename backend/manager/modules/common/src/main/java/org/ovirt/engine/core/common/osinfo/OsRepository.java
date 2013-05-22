package org.ovirt.engine.core.common.osinfo;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.compat.Version;

/**
 * Interface for accessing all Virtual OSs information.
 */
public interface OsRepository {

    public final static int DEFAULT_OS = 0;

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
     * @return minimum RAM in mb
     */
    public int getMinimumRam(int osId, Version version);

    /**
     * @return maximum RAM in mb
     */
    public int getMaximumRam(int osId, Version version);

    /**
     * @return if that OS could be connected with SPICE
     */
    public boolean hasSpiceSupport(int osId, Version version);

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
     * @return list of supported network devices
     */
    ArrayList<String> getNetworkDevices(int osId, Version version);

    /**
     * @param osId
     * @param version
     * @return a specific sound device for the given os.
     */
    String getSoundDevice(int osId, Version version);

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
     * Helper method to retire the old hard-code mapping of OsName to OS_NAME.<br>
     * As time goes by more OSs will be added, specifically by admins which the<br>
     * code is agnostic of and this method serves as the conventional way to keep<br>
     * this behaviour backward compatible.<br>
     *
     * @param name os name as represented by the namespace os.$osname e.g os.rhel6 name is rhel6<br>
     * @return the first digit met or first camel-cased word is getted separated by an underscore (_) or the original
     *         name otherwise<br>
     *         <p/>
     *         <pre>
     *                rhel6       -> RHEL_6 <br>
     *                rhel6x64    -> RHEL_6X64  <br>
     *                otherLinux  -> OTHER_LINUX <br>
     *         </pre>
     */
    String osNameUpperCasedAndUnderscored(String name);
}
