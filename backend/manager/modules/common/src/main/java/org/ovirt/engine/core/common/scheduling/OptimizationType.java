package org.ovirt.engine.core.common.scheduling;


/**
 * cluster's optimization method,
 * <p>
 * none - 0
 * optimize_for_speed - 1
 */
public enum OptimizationType {
    NONE(0),
    OPTIMIZE_FOR_SPEED(1);

    private final int value;

    OptimizationType(int value) {
        this.value = value;
    }

    public static OptimizationType from(int value) {
        for (OptimizationType v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
