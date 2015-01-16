package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;

public class ScheduleGlusterVolumeSnapshotParameters extends GlusterVolumeParameters {
    private static final long serialVersionUID = 1L;

    private GlusterVolumeSnapshotSchedule schedule;

    private boolean force;

    public ScheduleGlusterVolumeSnapshotParameters() {
    }

    public ScheduleGlusterVolumeSnapshotParameters(GlusterVolumeSnapshotSchedule schedule,
            boolean force) {
        super(schedule.getVolumeId());
        this.schedule = schedule;
        this.force = force;
    }

    public GlusterVolumeSnapshotSchedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(GlusterVolumeSnapshotSchedule schedule) {
        this.schedule = schedule;
    }

    public boolean getForce() {
        return this.force;
    }

    public void setForce(boolean value) {
        this.force = value;
    }
}
