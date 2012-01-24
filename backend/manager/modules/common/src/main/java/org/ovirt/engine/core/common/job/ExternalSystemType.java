package org.ovirt.engine.core.common.job;

public enum ExternalSystemType {
    VDSM;

    static public ExternalSystemType safeValueOf(String value) {
        if (value == null) {
            return null;
        }
        return ExternalSystemType.valueOf(value);
    }
}
