package org.ovirt.engine.core.common.mode;

/**
 * Represents different modes of the application.
 * Each mode is represented by a unique binary number.
 * <p>
 * VirtOnly - 0000 0001 (1), GlusterOnly - 0000 0010 (2)<br/>
 * </p>
 * Value for the new modes should be a power of 2. Example: QuantomOnly - 0000 0100 (4)
 */
public enum ApplicationMode {

    VirtOnly(1),
    GlusterOnly(2),
    AllModes(255);

    private final int value;

    ApplicationMode(int value) {
        this.value = value;
    }

    public static ApplicationMode from(int value) {
        for (ApplicationMode m : values()) {
            if (m.value == value) {
                return m;
            }
        }

        return null;
    }

    public int getValue() {
        return value;
    }

}
