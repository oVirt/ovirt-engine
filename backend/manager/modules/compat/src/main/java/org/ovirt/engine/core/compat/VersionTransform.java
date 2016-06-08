package org.ovirt.engine.core.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Version transformation.
 *
 * @author wxt
 *
 */
public class VersionTransform {

    private static Map<Version, Version> versionMap;

    private static void initVersionMap() {
        // Version[] ovirtVersions = { new Version(3, 3), new Version(3, 5), new Version(3, 6) };
        Version[] ovirtVersions = { Version.v3_3, Version.v3_5, Version.v3_6 };
        Version[] eayunVersions = { new Version(4, 0), new Version(4, 1), new Version(4, 2) };
        versionMap = new HashMap<Version, Version>();
        // 3.3 -> 4.0, 3.5 -> 4.1, 3.6 -> 4.2
        for (int i = 0; i < ovirtVersions.length; i++) {
            versionMap.put(ovirtVersions[i], eayunVersions[i]);
        }
    }

    public static Map<Version, Version> getVersionMap() {
        if (versionMap == null) {
            initVersionMap();
        }
        return versionMap;
    }

    public static Version getEayunVersion(Version ovirtVersion) {
        if (ovirtVersion != null && getVersionMap().containsKey(ovirtVersion)) {
            return getVersionMap().get(ovirtVersion);
        } else {
            return new Version();
        }
    }

    public static ArrayList<Version> cutVersionList(ArrayList<Version> versionList) {
        ArrayList<Version> resList = new ArrayList<Version>();
        if (versionList != null) {
            for (Version version : versionList) {
                if (Version.v3_6.equals(version)) {
                    resList.add(version);
                } else if (Version.v3_5.equals(version)) {
                    resList.add(version);
                } else if (Version.v3_3.equals(version)) {
                    resList.add(version);
                }
            }
            return resList;
        }
        return versionList;
    }

}
