package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static Map<String, NumaTuneMode> mappings =
            Stream.of(values()).collect(Collectors.toMap(NumaTuneMode::getValue, Function.identity()));

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
