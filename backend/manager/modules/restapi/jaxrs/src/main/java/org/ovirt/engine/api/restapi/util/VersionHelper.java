package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.api.model.Version;

public class VersionHelper {
    /**
     * Parse String to SystemVersion
     *
     * @param text {"Major.Minor.Build.Revision"}
     * @return SystemVersion
     */
    public static Version parseVersion(String text) {
        Version version = new Version();
        String[] parts = text.split("\\.", -1);
        switch (parts.length) {
        case 4:
            version.setRevision(Integer.parseInt(parts[3]));
        case 3:
            version.setBuild(Integer.parseInt(parts[2]));
        case 2:
            version.setMinor(Integer.parseInt(parts[1]));
        case 1:
            version.setMajor(Integer.parseInt(parts[0]));
        }
        return version;
    }

    public static boolean equals(Version v1, Version v2) {
        return v1.getMajor() != null && v1.getMajor().equals(v2.getMajor()) &&
                v1.getMinor() != null && v1.getMinor().equals(v2.getMinor());
    }
}
