package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class ScheduleGlusterVolumeSnapshotCommand extends ScheduleGlusterVolumeSnapshotCommandBase<ScheduleGlusterVolumeSnapshotParameters> {
    public ScheduleGlusterVolumeSnapshotCommand(ScheduleGlusterVolumeSnapshotParameters params) {
        super(params);
    }

    @Override
    protected void executeCommand() {
        // Keep a copy of the execution time before conversion to engine time zone during scheduling
        Time originalExecutionTime = getSchedule().getExecutionTime();

        // schedule the snapshot creation task
        String jobId = scheduleJob();

        if (jobId != null) {
            setSucceeded(true);
            getSchedule().setJobId(jobId);
            // reverting to original execution time in UI populated time zone
            getSchedule().setExecutionTime(originalExecutionTime);
            getGlusterVolumeSnapshotScheduleDao().save(getSchedule());
        } else {
            setSucceeded(false);
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        GlusterVolumeSnapshotSchedule fetchedSchedule =
                getGlusterVolumeSnapshotScheduleDao().getByVolumeId(getGlusterVolumeId());
        if (fetchedSchedule != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_ALREADY_SCHEDULED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULED;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_FAILED : errorType;
        }
    }
}
