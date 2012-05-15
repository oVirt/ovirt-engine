package org.ovirt.engine.ui.uicommonweb.uimode;

/**
 * Represents different UI modes of the application.
 *
 * Each mode is represented by a unique binary number.
 *
 * <p>
 * VirtOnly - 0000 0001 (1), GlusterOnly - 0000 0010 (2)<br/>
 * </p>
 * Value for the new modes should be a power of 2. Example: QuantomOnly - 0000 0100 (4)
 */
public enum UiMode {

    VirtOnly(1),
    GlusterOnly(2),
    VirtGluster(3),
    AllModes(255);

    private final int value;

    UiMode(int value) {
        this.value = value;
    }

    public static UiMode from(int value) {
        for (UiMode m : values()) {
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
