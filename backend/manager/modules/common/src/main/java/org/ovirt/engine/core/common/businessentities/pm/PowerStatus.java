package org.ovirt.engine.core.common.businessentities.pm;

/**
 * Power status reported by fence agent(s)
 */
public enum PowerStatus {
    /**
     * Host is turned on
     */
    ON,

    /**
     * Host is turned off
     */
    OFF,

    /**
     * Host status is unknown
     */
    UNKNOWN;

    /**
     * Parses power status from string
     */
    public static PowerStatus forValue(String value) {
        if ("on".equalsIgnoreCase(value)) {
            return ON;
        } else if ("off".equalsIgnoreCase(value)) {
            return OFF;
        } else {
            return UNKNOWN;
        }
    }
}

