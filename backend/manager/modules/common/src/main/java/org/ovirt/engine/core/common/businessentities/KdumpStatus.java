package org.ovirt.engine.core.common.businessentities;

/**
 * Status of kdump configuration
 */
public enum KdumpStatus {
    UNKNOWN(-1),
    DISABLED(0),
    ENABLED(1);

    /**
     * External numeric representation
     */
    private int value;

    private KdumpStatus(int value) {
        this.value = value;
    }

    public int getAsNumber() {
        return value;
    }

    /**
     * Converts numeric representation to enum value.
     *
     * @param value
     *            numeric representation
     * @return enum value ({@code null} or undefined numeric representation are converted to {@code UNKNOWN}
     */
    public static KdumpStatus valueOfNumber(Integer value) {
        KdumpStatus result = UNKNOWN;
        if (value != null) {
            for (KdumpStatus s : KdumpStatus.values()) {
                if (s.getAsNumber() == value) {
                    result = s;
                    break;
                }
            }
        }
        return result;
    }
}
