package org.ovirt.engine.core.common.businessentities;

public enum VmWatchdogType {
    i6300esb,
    diag288;

    public static VmWatchdogType getByName(String name) {
        if (name == null || name.length() == 0) {
            return null;
        } else {
            return VmWatchdogType.valueOf(name);
        }
    }
}
