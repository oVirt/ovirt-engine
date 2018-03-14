package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static Map<Integer, MigrationSupport> mappings =
            Stream.of(values()).collect(Collectors.toMap(MigrationSupport::getValue, Function.identity()));

    MigrationSupport(int value) {
        this.value = value;
    }

    public static MigrationSupport forValue(int value) {
        return mappings.get(value);
    }

    public int getValue() {
        return value;
    }
}
