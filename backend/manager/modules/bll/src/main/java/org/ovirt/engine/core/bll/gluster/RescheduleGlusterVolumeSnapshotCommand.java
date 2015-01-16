package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.timer.DBSchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

public class RescheduleGlusterVolumeSnapshotCommand extends ScheduleGlusterVolumeSnapshotCommandBase<ScheduleGlusterVolumeSnapshotParameters> {
    public RescheduleGlusterVolumeSnapshotCommand(ScheduleGlusterVolumeSnapshotParameters params) {
        super(params);
    }

    @Override
    protected void executeCommand() {
        Guid volumeId = getGlusterVolumeId();

        GlusterVolumeSnapshotSchedule fetchedSchedule = getGlusterVolumeSnapshotScheduleDao().getByVolumeId(volumeId);
        String jobId = fetchedSchedule.getJobId();
        SchedulerUtil scheduler = DBSchedulerUtilQuartzImpl.getInstance();

        // delete the existing job
        scheduler.deleteJob(jobId);

        GlusterVolumeSnapshotSchedule schedule = getSchedule();
        if (schedule.getRecurrence() != null) {
            // Keep a copy of the execution time before conversion to engine time zone
            Time originalExecutionTime = schedule.getExecutionTime();

            String newJobId = scheduleJob();

            if (newJobId != null) {
                setSucceeded(true);
                schedule.setJobId(newJobId);
                // reverting to original execution time in UI populated time zone
                schedule.setExecutionTime(originalExecutionTime);
                getGlusterVolumeSnapshotScheduleDao().updateScheduleByVolumeId(volumeId, schedule);
            } else {
                setSucceeded(false);
            }
        } else {
            getGlusterVolumeSnapshotScheduleDao().removeByVolumeId(volumeId);
            setSucceeded(true);
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        GlusterVolumeSnapshotSchedule fetchedSchedule =
                getGlusterVolumeSnapshotScheduleDao().getByVolumeId(getGlusterVolumeId());
        if (fetchedSchedule == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_NOT_SCHEDULED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESCHEDULED;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESCHEDULE_FAILED : errorType;
        }
    }
}
