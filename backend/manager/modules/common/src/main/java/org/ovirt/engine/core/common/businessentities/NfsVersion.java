package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum NfsVersion {
    // The value is used as both a vdsm parameter when mounting NFS
    // volumes as well as a string in the database
    AUTO("auto"),
    V3("3"),
    V4("4"),
    V4_0("4.0"),
    V4_1("4.1"),
    V4_2("4.2");

    private final String version;
    private static final Map<String, NfsVersion> mappings = new HashMap<>();

    static {
        for (NfsVersion nfsVersion : values()) {
            mappings.put(nfsVersion.getValue(), nfsVersion);
        }
    }

    private NfsVersion(String version) {
        this.version = version;
    }

    public static NfsVersion forValue(String value) {
        return mappings.get(value);
    }

    public String getValue() {
        return version;
    }
}
