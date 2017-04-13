package org.ovirt.engine.core.common.businessentities;

public enum VmExitStatus {
    Normal,
    Error,
    /** This status is set for highly available VMs (with no vm lease) after restoring a backup of
     *  the database and is not being reported by VDSM. see https://bugzilla.redhat.com/1441322 */
    Unknown;

    public int getValue() {
        return this.ordinal();
    }

    public static VmExitStatus forValue(int value) {
        return values()[value];
    }
}
