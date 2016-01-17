package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ScheduleGlusterVolumeSnapshotCommand extends ScheduleGlusterVolumeSnapshotCommandBase<ScheduleGlusterVolumeSnapshotParameters> {

    public ScheduleGlusterVolumeSnapshotCommand(ScheduleGlusterVolumeSnapshotParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void executeCommand() {
        // Check and disable the gluster CLI based snapshot scheduling first
        if (!checkAndDisableCliScheduler()) {
            setSucceeded(false);
            return;
        }


        // Keep a copy of the execution time before conversion to engine time zone during scheduling
        Time originalExecutionTime = getSchedule().getExecutionTime();
        // schedule the snapshot creation task
        try {
            String jobId = scheduleJob();
            setSucceeded(true);
            getSchedule().setJobId(jobId);
            // reverting to original execution time in UI populated time zone
            getSchedule().setExecutionTime(originalExecutionTime);
            getGlusterVolumeSnapshotScheduleDao().save(getSchedule());
        } catch (Exception ex) {
            setSucceeded(false);
            handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_FAILED, ex.getMessage());
        }
    }

    private boolean checkAndDisableCliScheduler() {
        GlusterVolumeEntity metaVolume =
                getGlusterVolumeDao().getByName(getClusterId(),
                        Config.<String> getValue(ConfigValues.GlusterMetaVolumeName));
        Cluster cluster = getCluster();
        if (metaVolume != null && cluster.isGlusterCliBasedSchedulingOn()) {
            VdcReturnValueBase returnValue =
                    runInternalAction(VdcActionType.DisableGlusterCliSnapshotScheduleInternal,
                            new GlusterVolumeActionParameters(getGlusterVolumeId(), true));
            if (!returnValue.getSucceeded()) {
                handleVdsErrors(AuditLogType.GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLE_FAILED,
                        returnValue.getExecuteFailedMessages());
            }
            return returnValue.getSucceeded();
        }

        return true;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        GlusterVolumeSnapshotSchedule fetchedSchedule =
                getGlusterVolumeSnapshotScheduleDao().getByVolumeId(getGlusterVolumeId());
        if (fetchedSchedule != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_ALREADY_SCHEDULED);
        }

        if (!getParameters().getForce()) {
            if (getGlusterVolumeDao().getByName(getClusterId(),
                    Config.<String> getValue(ConfigValues.GlusterMetaVolumeName)) != null
                    && getCluster().isGlusterCliBasedSchedulingOn()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_CLI_SCHEDULING_ENABLED);
            }
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
