package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Nameable;

public enum CinderVolumeStatus implements Nameable {
    Creating("creating"),
    Available("available"),
    Attaching("attaching"),
    InUse("in-use"),
    Deleting("deleting"),
    Error("error"),
    ErrorDeleting("error_deleting"),
    BackingUp("backing-up"),
    RestoringBackup("restoring-backup"),
    ErrorRestoring("error_restoring"),
    Extending("extending"),
    ErrorExtending("error_extending");

    private String name;
    private static Map<String, CinderVolumeStatus> mappings;

    static {
        mappings = new HashMap<>();
        for (CinderVolumeStatus status : values()) {
            mappings.put(status.getName(), status);
        }
    }

    CinderVolumeStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CinderVolumeStatus forValue(String name) {
        return mappings.get(name);
    }
}
