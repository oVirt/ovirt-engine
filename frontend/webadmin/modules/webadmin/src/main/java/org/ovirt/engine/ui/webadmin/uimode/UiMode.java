package org.ovirt.engine.ui.webadmin.uimode;

/**
 * Represents different UI modes of the application.
 */
public enum UiMode {

    VirtOnly("1"), //$NON-NLS-1$
    GlusterOnly("2"), //$NON-NLS-1$
    VirtGluster("3"); //$NON-NLS-1$

    private final String value;

    UiMode(String value) {
        this.value = value;
    }

    public static UiMode from(String value) {
        for (UiMode m : values()) {
            if (m.value.equals(value)) {
                return m;
            }
        }

        return null;
    }

}
