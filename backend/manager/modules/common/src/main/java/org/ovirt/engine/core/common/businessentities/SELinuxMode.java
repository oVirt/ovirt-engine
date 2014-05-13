package org.ovirt.engine.core.common.businessentities;

public enum SELinuxMode {
    ENFORCING(1), PERMISSIVE(0), DISABLED(-1);

    private int intValue;

    SELinuxMode(int i) {
        intValue = i;
    }

    public int toInt() {
        return intValue;
    }

    public static SELinuxMode fromValue(Integer v) {
        try {
            if (v==null) {
                return null;
            }
            if (v.equals(1)) {
                return ENFORCING;
            } else if (v.equals(0)) {
                return PERMISSIVE;
            } else if (v.equals(-1)) {
                return DISABLED;
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
