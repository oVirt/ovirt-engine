package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

/**
 * Libvirt numatune mode definition See http://libvirt.org/formatdomain.html#elementsNUMATuning
 *
 */
public enum NumaTuneMode {

    /**
     * Libvirt numatune memory mode 'strict'
     */
    STRICT,

    /**
     * Libvirt numatune memory mode 'interleave'
     */
    INTERLEAVE,

    /**
     * Libvirt numatune memory mode 'preferred'
     */
    PREFERRED;

    private String value;
    private static Map<String, NumaTuneMode> mappings;

    static {
        mappings = new HashMap<>();
        for (NumaTuneMode mode : values()) {
            mappings.put(mode.getValue(), mode);
        }
    }

    private NumaTuneMode() {
        value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public static NumaTuneMode forValue(String value) {
        return mappings.get(value);
    }
}
