package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class RescheduleGlusterVolumeSnapshotCommand extends ScheduleGlusterVolumeSnapshotCommandBase<ScheduleGlusterVolumeSnapshotParameters> {

    public RescheduleGlusterVolumeSnapshotCommand(ScheduleGlusterVolumeSnapshotParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void executeCommand() {
        Guid volumeId = getGlusterVolumeId();

        GlusterVolumeSnapshotSchedule fetchedSchedule = glusterVolumeSnapshotScheduleDao.getByVolumeId(volumeId);
        Guid jobId = fetchedSchedule.getJobId();

        // delete the existing job
        getDbSchedulUtil().deleteScheduledJob(jobId);

        GlusterVolumeSnapshotSchedule schedule = getSchedule();
        if (schedule.getRecurrence() != null) {
            // Keep a copy of the execution time before conversion to engine time zone
            Time originalExecutionTime = schedule.getExecutionTime();

            try {
                Guid newJobId = scheduleJob();
                setSucceeded(true);
                schedule.setJobId(newJobId);
                // reverting to original execution time in UI populated time zone
                schedule.setExecutionTime(originalExecutionTime);
                glusterVolumeSnapshotScheduleDao.updateScheduleByVolumeId(volumeId, schedule);
            } catch (Exception ex) {
                setSucceeded(false);
                handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESCHEDULE_FAILED, ex.getMessage());
            }
        } else {
            glusterVolumeSnapshotScheduleDao.removeByVolumeId(volumeId);
            setSucceeded(true);
        }
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        GlusterVolumeSnapshotSchedule fetchedSchedule =
                glusterVolumeSnapshotScheduleDao.getByVolumeId(getGlusterVolumeId());
        if (fetchedSchedule == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_NOT_SCHEDULED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (getSchedule().getRecurrence().equals(GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN)) {
                return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_DELETED;
            } else {
                return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESCHEDULED;
            }
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESCHEDULE_FAILED : errorType;
        }
    }
}
