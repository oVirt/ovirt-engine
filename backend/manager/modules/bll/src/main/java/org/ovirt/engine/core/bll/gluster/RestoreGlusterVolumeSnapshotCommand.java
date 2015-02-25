package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RestoreGlusterVolumeSnapshotCommand extends GlusterVolumeSnapshotCommandBase<GlusterVolumeSnapshotActionParameters> {
    private List<GlusterGeoRepSession> georepSessions;

    public RestoreGlusterVolumeSnapshotCommand(GlusterVolumeSnapshotActionParameters params) {
        super(params);
        georepSessions = getDbFacade().getGlusterGeoRepDao().getGeoRepSessions(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESTORE);
    }

    private boolean stopGeoReplicationSessions(List<GlusterGeoRepSession> geoRepSessions) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            try (EngineLock lock = acquireGeoRepSessionLock(session.getId())) {
                VdcReturnValueBase retVal = runInternalAction(VdcActionType.StopGeoRepSession,
                        new GlusterVolumeGeoRepSessionParameters(getGlusterVolumeId(), session.getId()));

                if (!retVal.getSucceeded()) {
                    handleVdsError(AuditLogType.GEOREP_SESSION_STOP_FAILED, retVal.getExecuteFailedMessages()
                            .toString());
                    setSucceeded(false);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean stopVolume(GlusterVolumeEntity volume) {
        if (volume != null && volume.getStatus() == GlusterStatus.UP) {
            VdcReturnValueBase retVal =
                    runInternalAction(VdcActionType.StopGlusterVolume,
                            new GlusterVolumeActionParameters(volume.getId(), true));
            if (!retVal.getSucceeded()) {
                handleVdsError(AuditLogType.GLUSTER_VOLUME_STOP_FAILED, retVal.getExecuteFailedMessages()
                        .toString());
                setSucceeded(false);
                return false;
            }
        }

        return true;
    }

    private boolean stopSlaveVolumes(List<GlusterGeoRepSession> geoRepSessions) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            GlusterVolumeEntity slaveVolume = getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
            if (slaveVolume == null) {
                // continue with other sessions and try to stop
                continue;
            }

            try (EngineLock lock = acquireEngineLock(session.getSlaveVolumeId(), LockingGroup.GLUSTER)) {
                if (!stopVolume(slaveVolume)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean restoreVolumeToSnapshot(Guid upServerId, GlusterVolumeEntity volume, String snapshotName) {
        if (volume != null) {
            VDSReturnValue retVal =
                    runVdsCommand(VDSCommandType.RestoreGlusterVolumeSnapshot,
                            new GlusterVolumeSnapshotActionVDSParameters(upServerId, volume.getName(), snapshotName));
            if (!retVal.getSucceeded()) {
                handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED, retVal.getVdsError()
                        .getMessage());
                setSucceeded(false);
                return false;
            } else {
                getGlusterVolumeSnapshotDao().removeByName(volume.getId(), snapshotName);
            }
        }

        return true;
    }

    private boolean restoreSlaveVolumesToSnapshot(List<GlusterGeoRepSession> geoRepSessions, String snapshotName) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            GlusterVolumeEntity slaveVolume = getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
            if (slaveVolume == null) {
                // continue with other sessions and try to pause
                continue;
            }

            VDS slaveUpServer = ClusterUtils.getInstance().getRandomUpServer(slaveVolume.getClusterId());
            if (slaveUpServer == null) {
                handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED,
                        VdcBllErrors.NoUpServerFoundInRemoteCluster.name());
                setSucceeded(false);
                return false;
            }

            try (EngineLock lock = acquireEngineLock(session.getSlaveVolumeId(), LockingGroup.GLUSTER_SNAPSHOT)) {
                if (!restoreVolumeToSnapshot(slaveUpServer.getId(), slaveVolume, getSnapshot().getSnapshotName())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean startVolume(Guid clusterId, Guid volumeId) {
        VdcReturnValueBase retVal =
                runInternalAction(VdcActionType.StartGlusterVolume, new GlusterVolumeActionParameters(volumeId,
                        true));

        if (!retVal.getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_START_FAILED, retVal.getExecuteFailedMessages().toString());
            setSucceeded(false);
            return false;
        }

        return true;
    }

    private boolean startSlaveVolumes(List<GlusterGeoRepSession> geoRepSessions) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            GlusterVolumeEntity slaveVolume = getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
            if (slaveVolume == null) {
                // continue with other sessions and try to stop
                continue;
            }

            try (EngineLock lock = acquireEngineLock(slaveVolume.getClusterId(), LockingGroup.GLUSTER)) {
                if (!startVolume(slaveVolume.getClusterId(), slaveVolume.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean resumeGeoRepSessions(List<GlusterGeoRepSession> geoRepSessions) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            GlusterVolumeEntity slaveVolume = getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
            if (slaveVolume == null) {
                // continue with other sessions and try to pause
                continue;
            }

            try (EngineLock lock = acquireGeoRepSessionLock(session.getId())) {
                VdcReturnValueBase retVal = runInternalAction(VdcActionType.ResumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionParameters(getGlusterVolumeId(), session.getId()));

                if (!retVal.getSucceeded()) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_GEO_REP_RESUME_FAILED, retVal.getExecuteFailedMessages()
                            .toString());
                    setSucceeded(false);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean startGeoRepSessions(List<GlusterGeoRepSession> geoRepSessions) {
        for (GlusterGeoRepSession session : geoRepSessions) {
            try (EngineLock lock = acquireGeoRepSessionLock(session.getId())) {
                VdcReturnValueBase retVal = runInternalAction(VdcActionType.StartGlusterVolumeGeoRep,
                        new GlusterVolumeGeoRepSessionParameters(getGlusterVolumeId(), session.getId()));

                if (!retVal.getSucceeded()) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_GEO_REP_START_FAILED_EXCEPTION,
                            retVal.getExecuteFailedMessages()
                                    .toString());
                    setSucceeded(false);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void executeCommand() {
        Boolean tranRetVal = TransactionSupport.executeInNewTransaction(new TransactionMethod<Boolean>() {
            @Override
            public Boolean runInTransaction() {
                if (georepSessions != null) {
                    // Pause the geo-replication session
                    if (!stopGeoReplicationSessions(georepSessions)) {
                        return false;
                    }

                    // Stop the slave volumes
                    if (!stopSlaveVolumes(georepSessions)) {
                        return false;
                    }

                    // Restore the slave volumes to said the snapshot
                    if (!restoreSlaveVolumesToSnapshot(georepSessions, getParameters().getSnapshotName())) {
                        return false;
                    }
                }

                return true;
            }
        });

        if (!tranRetVal) {
            return;
        }

        // Stop the master volume
        if (!stopVolume(getGlusterVolume())) {
            if (!georepSessions.isEmpty()) {
                handleVdsError(AuditLogType.GLUSTER_MASTER_VOLUME_STOP_FAILED_DURING_SNAPSHOT_RESTORE,
                        VdcBllErrors.FailedToStopMasterVolumeDuringVolumeSnapshotRestore.name());
            }
            return;
        }

        // Restore the master volume to the said snapshot
        if (!restoreVolumeToSnapshot(upServer.getId(), getGlusterVolume(), getParameters().getSnapshotName())) {
            if (!georepSessions.isEmpty()) {
                handleVdsError(AuditLogType.GLUSTER_MASTER_VOLUME_SNAPSHOT_RESTORE_FAILED,
                        VdcBllErrors.FailedToRestoreMasterVolumeDuringVolumeSnapshotRestore.name());
            }
            return;
        }

        List<GlusterGeoRepSession> updatedGeoRepSessions =
                getDbFacade().getGlusterGeoRepDao().getGeoRepSessions(getGlusterVolumeId());

        // Start the slave volumes
        if (updatedGeoRepSessions != null && !startSlaveVolumes(updatedGeoRepSessions)) {
            return;
        }

        // Start the master volume
        if (!startVolume(getGlusterVolume().getClusterId(), getGlusterVolumeId())) {
            return;
        }

        if (updatedGeoRepSessions != null) {
            // Start the geo-replication sessions
            if (!startGeoRepSessions(updatedGeoRepSessions)) {
                return;
            }

            // Resume the geo-replication sessions
            if (!resumeGeoRepSessions(updatedGeoRepSessions)) {
                return;
            }
        }

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        for (GlusterGeoRepSession session : georepSessions) {
            if (session.getSlaveVolumeId() == null || session.getSlaveNodeUuid() == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REMOTE_CLUSTER_NOT_MAINTAINED_BY_ENGINE);
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORED;
        } else {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED;
        }
    }
}
