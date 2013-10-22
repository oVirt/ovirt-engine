package org.ovirt.engine.ui.uicommonweb.models.vms;

public enum WANDisableEffects {
    animation("animation"), //$NON-NLS-1$
    wallpaper("wallpaper"), //$NON-NLS-1$
    fontSmooth("font-smooth"), //$NON-NLS-1$
    all("all"); //$NON-NLS-1$

    private final String stringValue;

    private WANDisableEffects(String stringValue) {
        this.stringValue = stringValue;
    }

    public static WANDisableEffects fromString(String str) {
        for (WANDisableEffects value : values()) {
            if (value.stringValue.equals(str)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Illegal string value: " + str);//$NON-NLS-1$
    }

    public String asString() {
        return stringValue;
    }
}
