package org.ovirt.engine.core.common.job;

public enum ExternalSystemType {
    VDSM,
    GLUSTER;

    public static ExternalSystemType safeValueOf(String value) {
        if (value == null) {
            return null;
        }
        return ExternalSystemType.valueOf(value);
    }
}
