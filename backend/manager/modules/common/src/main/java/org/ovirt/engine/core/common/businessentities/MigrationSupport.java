package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

/**
 * An enum for defining the migration support of the VM
 *
 *
 */
public enum MigrationSupport {
    MIGRATABLE(0), // can migrate with no restrictions
    IMPLICITLY_NON_MIGRATABLE(1), // user can cause migration using UI, the VM
                                  // will not be involved in any migration
                                  // that might
    // have been initiated by the server

    PINNED_TO_HOST(2); // can run only on the host that is set as "default host"

    private int value;
    private static HashMap<Integer, MigrationSupport> mappings;

    MigrationSupport(int value) {
        this.value = value;
    }

    static {
        mappings = new HashMap<>();

        for (MigrationSupport enumValue : MigrationSupport.values()) {
            mappings.put(enumValue.getValue(), enumValue);
        }
    }

    public static MigrationSupport forValue(int value) {
        return mappings.get(value);
    }

    public int getValue() {
        return value;
    }
}
