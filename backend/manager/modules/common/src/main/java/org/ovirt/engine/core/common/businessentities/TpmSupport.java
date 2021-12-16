package org.ovirt.engine.core.common.businessentities;

import java.util.stream.Stream;

public enum TpmSupport {

    UNSUPPORTED,
    SUPPORTED,
    REQUIRED;

    public static TpmSupport forValue(String value) {
        return Stream.of(values()).filter(v -> v.name().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

}
