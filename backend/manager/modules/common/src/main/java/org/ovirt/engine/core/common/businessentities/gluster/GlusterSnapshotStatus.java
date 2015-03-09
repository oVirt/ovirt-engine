package org.ovirt.engine.core.common.businessentities.gluster;

public enum GlusterSnapshotStatus {
    ACTIVATED,
    DEACTIVATED,
    UNKNOWN;

    public static GlusterSnapshotStatus from(String status) {
        for (GlusterSnapshotStatus snapshotStatus : values()) {
            if (snapshotStatus.name().equalsIgnoreCase(status)) {
                return snapshotStatus;
            }
        }

        return GlusterSnapshotStatus.UNKNOWN;
    }
}
