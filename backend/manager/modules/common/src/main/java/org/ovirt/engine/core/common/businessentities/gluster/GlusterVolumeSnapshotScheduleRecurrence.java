package org.ovirt.engine.core.common.businessentities.gluster;

public enum GlusterVolumeSnapshotScheduleRecurrence {
    INTERVAL,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    UNKNOWN;

    public static GlusterVolumeSnapshotScheduleRecurrence from(String value) {
        for (GlusterVolumeSnapshotScheduleRecurrence recurrence : values()) {
            if (recurrence.name().equals(value)) {
                return recurrence;
            }
        }

        return GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN;
    }
}
