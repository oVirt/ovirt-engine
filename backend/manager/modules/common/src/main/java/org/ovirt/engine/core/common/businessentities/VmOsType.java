package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmOsType implements Identifiable {
    Unassigned(0, OsType.Other, false),
    WindowsXP(1, OsType.Windows, false),
    Windows2003(3, OsType.Windows, false),
    Windows2008(4, OsType.Windows, false),
    OtherLinux(5, OsType.Linux, false),
    Other(6, OsType.Other, false),
    RHEL5(7, OsType.Linux, false),
    RHEL4(8, OsType.Linux, false),
    RHEL3(9, OsType.Linux, false),
    Windows2003x64(10, OsType.Windows, true),
    Windows7(11, OsType.Windows, false),
    Windows7x64(12, OsType.Windows, true),
    RHEL5x64(13, OsType.Linux, true),
    RHEL4x64(14, OsType.Linux, true),
    RHEL3x64(15, OsType.Linux, true),
    Windows2008x64(16, OsType.Windows, true),
    Windows2008R2x64(17, OsType.Windows, true),
    RHEL6(18, OsType.Linux, false),
    RHEL6x64(19, OsType.Linux, true),
    Windows8(20, OsType.Windows, false),
    Windows8x64(21, OsType.Windows, true),
    Windows2012x64(23, OsType.Windows, true);

    private final int intValue;
    private final OsType osType;
    private final boolean is64Bit;
    private final static java.util.HashMap<Integer, VmOsType> mappings = new HashMap<Integer, VmOsType>();

    static {
        for (VmOsType vmOsType : values()) {
            mappings.put(vmOsType.getValue(), vmOsType);
        }
    }

    private VmOsType(final int value, final OsType osType, final boolean is64Bit) {
        intValue = value;
        this.osType = osType;
        this.is64Bit = is64Bit;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public boolean getIs64Bit() {
        return (this.is64Bit);
    }

    public boolean isWindows() {
        return (this.osType == OsType.Windows);
    }

    public boolean isLinux() {
        return (this.osType == OsType.Linux);
    }

    public static VmOsType forValue(int value) {
        return mappings.get(value);
    }

    public OsType getOsType() {
        return osType;
    }
}
