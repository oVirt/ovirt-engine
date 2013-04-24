package org.ovirt.engine.core.common;

import org.ovirt.engine.core.common.businessentities.VmOsType;

public enum TimeZoneType {
    WINDOWS_TIMEZONE,
    GENERAL_TIMEZONE;

    public static TimeZoneType getTimeZoneByOs(VmOsType vmOsType) {
        return vmOsType.isWindows() ? WINDOWS_TIMEZONE : GENERAL_TIMEZONE;
    }
}
