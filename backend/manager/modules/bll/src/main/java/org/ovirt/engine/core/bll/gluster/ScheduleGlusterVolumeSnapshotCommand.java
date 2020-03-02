package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleGlusterVolumeSnapshotCommand extends ScheduleGlusterVolumeSnapshotCommandBase<ScheduleGlusterVolumeSnapshotParameters> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    public ScheduleGlusterVolumeSnapshotCommand(ScheduleGlusterVolumeSnapshotParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void executeCommand() {
        log.debug("Executing ScheduleGlusterVolumeSnapshotCommand ");
        // Check and disable the gluster CLI based snapshot scheduling first
        if (!checkAndDisableCliScheduler()) {
            setSucceeded(false);
            return;
        }

        // Keep a copy of the execution time before conversion to engine time zone during scheduling
        Time originalExecutionTime = getSchedule().getExecutionTime();
        log.debug("ScheduleGlusterVolumeSnapshotCommand--------originalExecutionTime {}", originalExecutionTime);
        // schedule the snapshot creation task
        try {
            Guid jobId = scheduleJob();
            setSucceeded(true);
            getSchedule().setJobId(jobId);
            // reverting to original execution time in UI populated time zone
            getSchedule().setExecutionTime(originalExecutionTime);
            glusterVolumeSnapshotScheduleDao.save(getSchedule());
        } catch (Exception ex) {
            setSucceeded(false);
            handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_FAILED, ex.getMessage());
        }
    }

    private boolean checkAndDisableCliScheduler() {
        GlusterVolumeEntity metaVolume =
                glusterVolumeDao.getByName(getClusterId(), Config.getValue(ConfigValues.GlusterMetaVolumeName));
        Cluster cluster = getCluster();
        if (metaVolume != null && cluster.isGlusterCliBasedSchedulingOn()) {
            ActionReturnValue returnValue =
                    runInternalAction(ActionType.DisableGlusterCliSnapshotScheduleInternal,
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
                glusterVolumeSnapshotScheduleDao.getByVolumeId(getGlusterVolumeId());
        if (fetchedSchedule != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_ALREADY_SCHEDULED);
        }

        if (!getParameters().getForce()) {
            if (glusterVolumeDao.getByName(getClusterId(), Config.getValue(ConfigValues.GlusterMetaVolumeName)) != null
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
